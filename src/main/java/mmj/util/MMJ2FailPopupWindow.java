//********************************************************************/
//* Copyright (C) 2011  MEL O'CAT  mmj2 (via) planetmath (dot) org   */
//* License terms: GNU General Public License Version 2              */
//*                or any later version                              */
//********************************************************************/
//*4567890123456 (71-character line to adjust editor window) 23456789*/

/*
 * MMJ2FailPopupWindow.java.java  0.01 11/01/2011
 *
 * Nov-01-2011 - Version 0.01
 *         --> new
 */

package mmj.util;

import javax.swing.JOptionPane;

import mmj.lang.Messages;
import mmj.pa.AuxFrameGUI;
import mmj.pa.PaConstants;

/**
 * {@code MMJ2FailPopupWindow} displays start-up and abnormal termination
 * errors.
 * <p>
 * Here is an overview of how this works:
 * <ol>
 * <li>{@code BatchFramework} obtains the {@code displayMMJ2FailPopupWindow}
 * parameter from the command line arguments. If "Y" or not entered (default),
 * and during command line processing, the {@code MMJ2FailPopupWindow} is used
 * to display fail (abort) errors and error messages generated during "startup"
 * -- meaning prior to the Proof Assistant GUI display.
 * <li>{@code BatchFramework.runIt()} checks for "fail" errors and calls
 * {@code MMJ2FailPopupWindow.displayFailMessage()} to display the final message
 * from mmj2 :-)
 * <li>{@code OutputBoss.printAndClearMessages()}, which is used by the various
 * {@code mmj.util.*Boss} classes to output errors generated by RunParm
 * processing, calls {@code accumStartupErrors()} and
 * {@code displayStartupErrors} in {@code MMJ2FailPopupWindow}. There are two
 * functions because we want the messages to also be output to the Command
 * Prompt window but that process deletes the messages. So we gather them first,
 * and then later display them.
 * <li>The other "tricky" thing is knowing what is a "startup" error message
 * versus a "non-startup" messages. For that the {@code startupMode} boolean
 * variable is used. Initially, right after the command line arguments are
 * parsed, {@code BatchFramework.runIt()} calls {@code initiateStartupMode()}
 * and then later, in {@code ProofAsstBoss.doRunProofAsstGUI()} calls
 * {@code terminateStartupMode()}.
 */
public class MMJ2FailPopupWindow {

    private final BatchFramework batchFramework;

    private boolean enabled;

    private boolean startupMode;

    private String startupErrors;

    private AuxFrameGUI auxFrameGUI;

    /**
     * Standard constructor.
     *
     * @param batchFramework The {@code BatchFramework} object.
     * @param enabled true to enable display of the popup window, otherwise
     *            false.
     */
    public MMJ2FailPopupWindow(final BatchFramework batchFramework,
        final boolean enabled)
    {

        this.batchFramework = batchFramework;
        setEnabled(enabled);
    }

    /**
     * Sets {@code enabled} switch to turn on/off display of the
     * {@code MMJ2FailPopupWindow}.
     *
     * @param enabled true to enable display of the popup window, otherwise
     *            false.
     */
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Sets startupMode to {@code true} and initializes related variables.
     */
    public void initiateStartupMode() {
        startupErrors = null;
        if (enabled)
            startupMode = true;
    }

    /**
     * Ends startupMode so that only Fail messages are shown.
     */
    public void terminateStartupMode() {
        startupMode = false;
        startupErrors = null;
    }

    /**
     * Displays a {@code JOptionPane} Dialog showing the {@code failMessage}.
     * <p>
     * Note: if {@code MMJ2FailPopupWindow} is not {@code enabled} the Dialog is
     * not shown.
     *
     * @param failMessage the final mmj2 message before abnormal termination of
     *            processing.
     */
    public void displayFailMessage(final String failMessage) {
        if (!enabled)
            return;

        showAuxFrameGUI();

        JOptionPane.showMessageDialog(auxFrameGUI.getFrame(),
            addLineBreaks(failMessage), UtilConstants.MMJ2_FAIL_DIALOG_TITLE
                + " " + batchFramework.getRunParmFileAbsolutePath(),

            JOptionPane.ERROR_MESSAGE);

        unshowAuxFrameGUI();
    }

    /**
     * Gathers the {@code Messages} error messages to be displayed.
     * <p>
     * Note: if {@code MMJ2FailPopupWindow} is not {@code enabled} or not in
     * {@code startupMode} or if the current RunParm command is
     * {@code VerifyProof} the messages are not accumulated (or shown).
     */
    public void accumStartupErrors() {
        startupErrors = null;
        if (!enabled || !startupMode
            || batchFramework.currentRunParmCommand == UtilConstants.RUNPARM_VERIFY_PROOF)
            return;

        final Messages messages = batchFramework.outputBoss.getMessages();

        final int msgCount = messages.getErrorMessageCnt();

        if (msgCount == 0)
            return;

        final StringBuilder sb = new StringBuilder(
            messages.getErrorMessageCnt() * 160); // guessing average message
                                                  // length

        final String[] msgArray = messages.getErrorMessageArray();

        for (int i = 0; i < msgCount
            && i < UtilConstants.MAX_STARTUP_ERROR_MESSAGES; i++)
        {
            sb.append(UtilConstants.NEW_LINE_CHAR);
            sb.append(addLineBreaks(msgArray[i]));
            sb.append(UtilConstants.NEW_LINE_CHAR);
        }
        startupErrors = UtilConstants.MMJ2_STARTUP_MSG_LIT_1
            + batchFramework.runParmCnt + UtilConstants.MMJ2_STARTUP_MSG_LIT_2
            + batchFramework.currentRunParmCommand
            + UtilConstants.MMJ2_STARTUP_MSG_LIT_3 + sb;
    }

    /**
     * Displays a Dialog showing the messages gathered by
     * {@code accumStartupErrors}.
     * <p>
     * Note: if {@code MMJ2FailPopupWindow} is not {@code enabled} or if no
     * error messages were gathered the Dialog is not shown.
     */
    public void displayStartupErrors() {
        if (!enabled || !startupMode || startupErrors == null)
            return;

        showAuxFrameGUI();

        JOptionPane.showMessageDialog(auxFrameGUI.getFrame(), startupErrors,
            UtilConstants.MMJ2_FAIL_STARTUP_DIALOG_TITLE + " "
                + batchFramework.getRunParmFileAbsolutePath(),
            JOptionPane.ERROR_MESSAGE);

        unshowAuxFrameGUI();
    }

    private String addLineBreaks(final String s) {
        final StringBuilder b = new StringBuilder(s.length() * 10 / 9);
        int unbrokenTextLength = 0;
        int pos = -1;
        boolean firstNonBlankAlreadyOutput = true;
        char c;
        while (++pos < s.length()) {
            c = s.charAt(pos);
            if (c == UtilConstants.NEW_LINE_CHAR) {
                b.append(c);
                unbrokenTextLength = 0;
                firstNonBlankAlreadyOutput = true; // honor leading spaces
                continue;
            }
            if (c == ' ') {
                if (!firstNonBlankAlreadyOutput)
                    // bypass leading space after inserted new line
                    continue;
                // can only insert new line at blank positions
                if (unbrokenTextLength >= UtilConstants.LINE_BREAK_MAX_LENGTH) {
                    b.append(UtilConstants.NEW_LINE_CHAR);
                    unbrokenTextLength = 0;
                    firstNonBlankAlreadyOutput = false;
                    continue;
                }
            }
            b.append(c);
            unbrokenTextLength++;
            firstNonBlankAlreadyOutput = true;
        }
        return b.toString();
    }

    // need a parent frame otherwise alt-tab while
    // dialog open leaves dialog missing-in-action
    // and no way to continue except to close the
    // command prompt window!
    private void showAuxFrameGUI() {

        auxFrameGUI = new AuxFrameGUI();

        auxFrameGUI.buildFrame();

        // might as well use the advertising space :-)
        auxFrameGUI.changeFrameText(PaConstants.PROOF_ASST_GUI_STARTUP_MSG);

        auxFrameGUI.showFrame(auxFrameGUI.getFrame());
    }

    private void unshowAuxFrameGUI() {
        auxFrameGUI.dispose();
    }

}
