//*****************************************************************************/
//* Copyright (C) 2005-2013                                                   */
//* MEL O'CAT  X178G243 (at) yahoo (dot) com                                  */
//* License terms: GNU General Public License Version 2                       */
//*                or any later version                                       */
//*****************************************************************************/
//*456789012345678 (80-character line to adjust editor window) 456789012345678*/

/*
 * SearchDataLines.java  0.01 20/09/2012
 *
 * Version 0.01:
 * Aug-09-2013: new from decompilation.
 */

package mmj.search;

import mmj.lang.Assrt;

public class SearchDataLines {

    public SearchDataLines(final CompiledSearchArgs csa) {
        getter = null;
        line = null;
        getter = new SearchDataGetter();
        line = new SearchDataLine[4];
        for (int i = 0; i < line.length; i++)
			line[i] = csa.searchForWhat[i].equals("") ? null : SearchDataLine.createSearchDataLine(csa, i, getter);

    }

    public boolean evaluate(final Assrt assrt, final CompiledSearchArgs csa) {
        getter.initForNextSearch(assrt);
        boolean flag = false;
        for (SearchDataLine searchDataLine : line) {
            if (searchDataLine == null)
                continue;
            final int i = searchDataLine.evaluate(csa);
            if (i == 0)
                continue;
            if (i > 0) {
                flag = true;
                if (!searchDataLine.isBoolSetToAnd())
                    break;
                continue;
            }
            flag = false;
            if (searchDataLine.isBoolSetToAnd())
                break;
        }

        return flag;
    }

    SearchDataGetter getter;
    SearchDataLine[] line;
}
