package org.json;

/*
 Copyright (c) 2002 JSON.org

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 The Software shall be used for Good, not Evil.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
import java.io.*;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * A JSONArray is an ordered sequence of values. Its external text form is a
 * string wrapped in square brackets with commas separating the values. The
 * internal form is an object having <code>get</code> and <code>opt</code>
 * methods for accessing the values by index, and <code>put</code> methods for
 * adding or replacing values. The values can be any of these types:
 * <code>Boolean</code>, <code>JSONArray</code>, <code>JSONObject</code>,
 * <code>Number</code>, <code>String</code>, or the
 * <code>JSONObject.NULL object</code>.
 * <p>
 * The constructor can convert a JSON text into a Java object. The
 * <code>toString</code> method converts to JSON text.
 * <p>
 * A <code>get</code> method returns a value if one can be found, and throws an
 * exception if one cannot be found. An <code>opt</code> method returns a
 * default value instead of throwing an exception, and so is useful for
 * obtaining optional values.
 * <p>
 * The generic <code>get()</code> and <code>opt()</code> methods return an
 * object which you can cast or query for type. There are also typed
 * <code>get</code> and <code>opt</code> methods that do type checking and type
 * coercion for you.
 * <p>
 * The texts produced by the <code>toString</code> methods strictly conform to
 * JSON syntax rules. The constructors are more forgiving in the texts they will
 * accept:
 * <ul>
 * <li>An extra <code>,</code>&nbsp;<small>(comma)</small> may appear just
 * before the closing bracket.</li>
 * <li>The <code>null</code> value will be inserted when there is <code>,</code>
 * &nbsp;<small>(comma)</small> elision.</li>
 * <li>Strings may be quoted with <code>'</code>&nbsp;<small>(single
 * quote)</small>.</li>
 * <li>Strings do not need to be quoted at all if they do not begin with a quote
 * or single quote, and if they do not contain leading or trailing spaces, and
 * if they do not contain any of these characters:
 * <code>{ } [ ] / \ : , #</code> and if they do not look like numbers and if
 * they are not the reserved words <code>true</code>, <code>false</code>, or
 * <code>null</code>.</li>
 * </ul>
 *
 * @author JSON.org
 * @version 2015-10-29
 */
public class JSONArray extends ArrayList<Object> {

    /**
     * Construct an empty JSONArray.
     */
    public JSONArray() {
        super();
    }

    /**
     * Construct a JSONArray from a JSONTokener.
     *
     * @param x A JSONTokener
     * @throws JSONException If there is a syntax error.
     */
    public JSONArray(final JSONTokener x) throws JSONException {
        this();
        if (x.nextClean() != '[')
            throw x.syntaxError("A JSONArray text must start with '['");
        if (x.nextClean() != ']') {
            x.back();
            for (;;) {
                if (x.nextClean() == ',') {
                    x.back();
                    super.add(JSONObject.NULL);
                }
                else {
                    x.back();
                    super.add(x.nextValue());
                }
                switch (x.nextClean()) {
                    case ',':
                        if (x.nextClean() == ']')
                            return;
                        x.back();
                        break;
                    case ']':
                        return;
                    default:
                        throw x.syntaxError("Expected a ',' or ']'");
                }
            }
        }
    }

    /**
     * Construct a JSONArray from a source JSON text.
     *
     * @param source A string that begins with <code>[</code>&nbsp;<small>(left
     *            bracket)</small> and ends with <code>]</code> &nbsp;
     *            <small>(right bracket)</small>.
     * @throws JSONException If there is a syntax error.
     */
    public JSONArray(final String source) throws JSONException {
        this(new JSONTokener(source));
    }

    /**
     * Construct a JSONArray from a Collection.
     *
     * @param collection A Collection.
     */
    public JSONArray(final Collection<?> collection) {
        this();
        if (collection != null)
            for (final Object o : collection)
                super.add(JSONObject.wrap(o));
    }

    /**
     * Construct a JSONArray from an array
     *
     * @param <T> The type of the array elements
     * @param array The array
     */
    @SafeVarargs
    public <T> JSONArray(final T... array) {
        this();
        for (final T x : array)
            this.put(JSONObject.wrap(x));
    }

    /**
     * Construct a JSONArray from an array
     *
     * @param array The array
     * @throws JSONException If not an array.
     */
    public JSONArray(final Object array) throws JSONException {
        this();
        if (array.getClass().isArray()) {
            final int length = Array.getLength(array);
            for (int i = 0; i < length; i += 1)
                this.put(JSONObject.wrap(Array.get(array, i)));
        }
        else
            throw new JSONException(
                "JSONArray initial value should be a string or collection or array.");
    }

    /**
     * Get the object value associated with an index.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return An object value.
     * @throws JSONException If there is no value for the index.
     */
    @Override
    public Object get(final int index) throws JSONException {
        final Object object = opt(index);
        if (object == null)
            throw new JSONException("JSONArray[" + index + "] not found.");
        return object;
    }

    /**
     * Get the boolean value associated with an index. The string values "true"
     * and "false" are converted to boolean.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return The truth.
     * @throws JSONException If there is no value for the index or if the value
     *             is not convertible to boolean.
     */
    public boolean getBoolean(final int index) throws JSONException {
        final Object object = get(index);
        if (object.equals(Boolean.FALSE) || object instanceof String
            && ((String)object).equalsIgnoreCase("false"))
            return false;
        else if (object.equals(Boolean.TRUE) || object instanceof String
            && ((String)object).equalsIgnoreCase("true"))
            return true;
        throw new JSONException("JSONArray[" + index + "] is not a boolean.");
    }

    /**
     * Get the double value associated with an index.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return The value.
     * @throws JSONException If the key is not found or if the value cannot be
     *             converted to a number.
     */
    public double getDouble(final int index) throws JSONException {
        final Object object = get(index);
        try {
            return object instanceof Number ? ((Number)object).doubleValue()
                : Double.parseDouble((String)object);
        } catch (final Exception e) {
            throw new JSONException(
                "JSONArray[" + index + "] is not a number.");
        }
    }

    /**
     * Get the enum value associated with an index.
     *
     * @param <E> The enum type
     * @param clazz The type of enum to retrieve.
     * @param index The index must be between 0 and length() - 1.
     * @return The enum value at the index location
     * @throws JSONException if the key is not found or if the value cannot be
     *             converted to an enum.
     */
    public <E extends Enum<E>> E getEnum(final Class<E> clazz, final int index)
        throws JSONException
    {
        final E val = optEnum(clazz, index);
        if (val == null)
            // JSONException should really take a throwable argument.
            // If it did, I would re-implement this with the Enum.valueOf
            // method and place any thrown exception in the JSONException
            throw new JSONException(
                "JSONObject[" + JSONObject.quote(Integer.toString(index))
                    + "] is not an enum of type "
                    + JSONObject.quote(clazz.getSimpleName()) + ".");
        return val;
    }

    /**
     * Get the BigDecimal value associated with an index.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return The value.
     * @throws JSONException If the key is not found or if the value cannot be
     *             converted to a BigDecimal.
     */
    public BigDecimal getBigDecimal(final int index) throws JSONException {
        final Object object = get(index);
        try {
            return new BigDecimal(object.toString());
        } catch (final Exception e) {
            throw new JSONException(
                "JSONArray[" + index + "] could not convert to BigDecimal.");
        }
    }

    /**
     * Get the BigInteger value associated with an index.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return The value.
     * @throws JSONException If the key is not found or if the value cannot be
     *             converted to a BigInteger.
     */
    public BigInteger getBigInteger(final int index) throws JSONException {
        final Object object = get(index);
        try {
            return new BigInteger(object.toString());
        } catch (final Exception e) {
            throw new JSONException(
                "JSONArray[" + index + "] could not convert to BigInteger.");
        }
    }

    /**
     * Get the int value associated with an index.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return The value.
     * @throws JSONException If the key is not found or if the value is not a
     *             number.
     */
    public int getInt(final int index) throws JSONException {
        final Object object = get(index);
        try {
            return object instanceof Number ? ((Number)object).intValue()
                : Integer.parseInt((String)object);
        } catch (final Exception e) {
            throw new JSONException(
                "JSONArray[" + index + "] is not a number.");
        }
    }

    /**
     * Get the JSONArray associated with an index.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return A JSONArray value.
     * @throws JSONException If there is no value for the index. or if the value
     *             is not a JSONArray
     */
    public JSONArray getJSONArray(final int index) throws JSONException {
        final Object object = get(index);
        if (object instanceof JSONArray)
            return (JSONArray)object;
        throw new JSONException("JSONArray[" + index + "] is not a JSONArray.");
    }

    /**
     * Get the JSONObject associated with an index.
     *
     * @param index subscript
     * @return A JSONObject value.
     * @throws JSONException If there is no value for the index or if the value
     *             is not a JSONObject
     */
    public JSONObject getJSONObject(final int index) throws JSONException {
        final Object object = get(index);
        if (object instanceof JSONObject)
            return (JSONObject)object;
        throw new JSONException(
            "JSONArray[" + index + "] is not a JSONObject.");
    }

    /**
     * Get the long value associated with an index.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return The value.
     * @throws JSONException If the key is not found or if the value cannot be
     *             converted to a number.
     */
    public long getLong(final int index) throws JSONException {
        final Object object = get(index);
        try {
            return object instanceof Number ? ((Number)object).longValue()
                : Long.parseLong((String)object);
        } catch (final Exception e) {
            throw new JSONException(
                "JSONArray[" + index + "] is not a number.");
        }
    }

    /**
     * Get the string associated with an index.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return A string value.
     * @throws JSONException If there is no string value for the index.
     */
    public String getString(final int index) throws JSONException {
        final Object object = get(index);
        if (object instanceof String)
            return (String)object;
        throw new JSONException("JSONArray[" + index + "] not a string.");
    }

    /**
     * Determine if the value is null.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return true if the value at the index is null, or if there is no value.
     */
    public boolean isNull(final int index) {
        return JSONObject.NULL.equals(opt(index));
    }

    /**
     * Make a string from the contents of this JSONArray. The
     * <code>separator</code> string is inserted between each element. Warning:
     * This method assumes that the data structure is acyclical.
     *
     * @param separator A string that will be inserted between the elements.
     * @return a string.
     * @throws JSONException If the array contains an invalid number.
     */
    public String join(final String separator) throws JSONException {
        final int len = length();
        final StringBuilder sb = new StringBuilder();

        for (int i = 0; i < len; i += 1) {
            if (i > 0)
                sb.append(separator);
            sb.append(JSONObject.valueToString(super.get(i)));
        }
        return sb.toString();
    }

    /**
     * Get the number of elements in the JSONArray, included nulls.
     *
     * @return The length (or size).
     */
    public int length() {
        return size();
    }

    /**
     * Get the optional object value associated with an index.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return An object value, or null if there is no object at that index.
     */
    public Object opt(final int index) {
        return index < 0 || index >= length() ? null : super.get(index);
    }

    /**
     * Get the optional boolean value associated with an index. It returns false
     * if there is no value at that index, or if the value is not Boolean.TRUE
     * or the String "true".
     *
     * @param index The index must be between 0 and length() - 1.
     * @return The truth.
     */
    public boolean optBoolean(final int index) {
        return this.optBoolean(index, false);
    }

    /**
     * Get the optional boolean value associated with an index. It returns the
     * defaultValue if there is no value at that index or if it is not a Boolean
     * or the String "true" or "false" (case insensitive).
     *
     * @param index The index must be between 0 and length() - 1.
     * @param defaultValue A boolean default.
     * @return The truth.
     */
    public boolean optBoolean(final int index, final boolean defaultValue) {
        try {
            return getBoolean(index);
        } catch (final Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get the optional double value associated with an index. NaN is returned
     * if there is no value for the index, or if the value is not a number and
     * cannot be converted to a number.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return The value.
     */
    public double optDouble(final int index) {
        return this.optDouble(index, Double.NaN);
    }

    /**
     * Get the optional double value associated with an index. The defaultValue
     * is returned if there is no value for the index, or if the value is not a
     * number and cannot be converted to a number.
     *
     * @param index subscript
     * @param defaultValue The default value.
     * @return The value.
     */
    public double optDouble(final int index, final double defaultValue) {
        try {
            return getDouble(index);
        } catch (final Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get the optional int value associated with an index. Zero is returned if
     * there is no value for the index, or if the value is not a number and
     * cannot be converted to a number.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return The value.
     */
    public int optInt(final int index) {
        return this.optInt(index, 0);
    }

    /**
     * Get the optional int value associated with an index. The defaultValue is
     * returned if there is no value for the index, or if the value is not a
     * number and cannot be converted to a number.
     *
     * @param index The index must be between 0 and length() - 1.
     * @param defaultValue The default value.
     * @return The value.
     */
    public int optInt(final int index, final int defaultValue) {
        try {
            return getInt(index);
        } catch (final Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get the enum value associated with a key.
     *
     * @param <E> The enum
     * @param clazz The type of enum to retrieve.
     * @param index The index must be between 0 and length() - 1.
     * @return The enum value at the index location or null if not found
     */
    public <E extends Enum<E>> E optEnum(final Class<E> clazz,
        final int index)
    {
        return this.optEnum(clazz, index, null);
    }

    /**
     * Get the enum value associated with a key.
     *
     * @param <E> The enum type
     * @param clazz The type of enum to retrieve.
     * @param index The index must be between 0 and length() - 1.
     * @param defaultValue The default in case the value is not found
     * @return The enum value at the index location or defaultValue if the value
     *         is not found or cannot be assigned to clazz
     */
    public <E extends Enum<E>> E optEnum(final Class<E> clazz, final int index,
        final E defaultValue)
    {
        try {
            final Object val = opt(index);
            if (JSONObject.NULL.equals(val))
                return defaultValue;
            if (clazz.isAssignableFrom(val.getClass())) {
                // we just checked it!
                @SuppressWarnings("unchecked")
                final E myE = (E)val;
                return myE;
            }
            return Enum.valueOf(clazz, val.toString());
        } catch (IllegalArgumentException | NullPointerException e) {
            return defaultValue;
        }
    }

    /**
     * Get the optional BigInteger value associated with an index. The
     * defaultValue is returned if there is no value for the index, or if the
     * value is not a number and cannot be converted to a number.
     *
     * @param index The index must be between 0 and length() - 1.
     * @param defaultValue The default value.
     * @return The value.
     */
    public BigInteger optBigInteger(final int index,
        final BigInteger defaultValue)
    {
        try {
            return getBigInteger(index);
        } catch (final Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get the optional BigDecimal value associated with an index. The
     * defaultValue is returned if there is no value for the index, or if the
     * value is not a number and cannot be converted to a number.
     *
     * @param index The index must be between 0 and length() - 1.
     * @param defaultValue The default value.
     * @return The value.
     */
    public BigDecimal optBigDecimal(final int index,
        final BigDecimal defaultValue)
    {
        try {
            return getBigDecimal(index);
        } catch (final Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get the optional JSONArray associated with an index.
     *
     * @param index subscript
     * @return A JSONArray value, or null if the index has no value, or if the
     *         value is not a JSONArray.
     */
    public JSONArray optJSONArray(final int index) {
        final Object o = opt(index);
        return o instanceof JSONArray ? (JSONArray)o : null;
    }

    /**
     * Get the optional JSONObject associated with an index. Null is returned if
     * the key is not found, or null if the index has no value, or if the value
     * is not a JSONObject.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return A JSONObject value.
     */
    public JSONObject optJSONObject(final int index) {
        final Object o = opt(index);
        return o instanceof JSONObject ? (JSONObject)o : null;
    }

    /**
     * Get the optional long value associated with an index. Zero is returned if
     * there is no value for the index, or if the value is not a number and
     * cannot be converted to a number.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return The value.
     */
    public long optLong(final int index) {
        return this.optLong(index, 0);
    }

    /**
     * Get the optional long value associated with an index. The defaultValue is
     * returned if there is no value for the index, or if the value is not a
     * number and cannot be converted to a number.
     *
     * @param index The index must be between 0 and length() - 1.
     * @param defaultValue The default value.
     * @return The value.
     */
    public long optLong(final int index, final long defaultValue) {
        try {
            return getLong(index);
        } catch (final Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get the optional string value associated with an index. It returns an
     * empty string if there is no value at that index. If the value is not a
     * string and is not null, then it is coverted to a string.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return A String value.
     */
    public String optString(final int index) {
        return this.optString(index, "");
    }

    /**
     * Get the optional string associated with an index. The defaultValue is
     * returned if the key is not found.
     *
     * @param index The index must be between 0 and length() - 1.
     * @param defaultValue The default value.
     * @return A String value.
     */
    public String optString(final int index, final String defaultValue) {
        final Object object = opt(index);
        return JSONObject.NULL.equals(object) ? defaultValue
            : object.toString();
    }

    /**
     * Append a boolean value. This increases the array's length by one.
     *
     * @param value A boolean value.
     * @return this.
     */
    public JSONArray put(final boolean value) {
        this.put(value ? Boolean.TRUE : Boolean.FALSE);
        return this;
    }

    /**
     * Put a value in the JSONArray, where the value will be a JSONArray which
     * is produced from a Collection.
     *
     * @param value A Collection value.
     * @return this.
     */
    public JSONArray put(final Collection<?> value) {
        return put((Object)(value instanceof JSONArray ? value
            : new JSONArray(value)));
    }

    /**
     * Append a double value. This increases the array's length by one.
     *
     * @param value A double value.
     * @throws JSONException if the value is not finite.
     * @return this.
     */
    public JSONArray put(final double value) throws JSONException {
        final Double d = value;
        JSONObject.testValidity(d);
        this.put(d);
        return this;
    }

    /**
     * Append an int value. This increases the array's length by one.
     *
     * @param value An int value.
     * @return this.
     */
    public JSONArray put(final int value) {
        this.put(Integer.valueOf(value));
        return this;
    }

    /**
     * Append an long value. This increases the array's length by one.
     *
     * @param value A long value.
     * @return this.
     */
    public JSONArray put(final long value) {
        this.put(Long.valueOf(value));
        return this;
    }

    /**
     * Put a value in the JSONArray, where the value will be a JSONObject which
     * is produced from a Map.
     *
     * @param value A Map value.
     * @return this.
     */
    public JSONArray put(final Map<?, ?> value) {
        return put((Object)(value instanceof JSONObject ? value
            : new JSONObject(value)));
    }

    /**
     * Append an object value. This increases the array's length by one.
     *
     * @param value An object value. The value should be a Boolean, Double,
     *            Integer, JSONArray, JSONObject, Long, or String, or the
     *            JSONObject.NULL object.
     * @return this.
     */
    public JSONArray put(final Object value) {
        super.add(value);
        return this;
    }

    /**
     * Put or replace a boolean value in the JSONArray. If the index is greater
     * than the length of the JSONArray, then null elements will be added as
     * necessary to pad it out.
     *
     * @param index The subscript.
     * @param value A boolean value.
     * @return this.
     * @throws JSONException If the index is negative.
     */
    public JSONArray put(final int index, final boolean value)
        throws JSONException
    {
        this.put(index, value ? Boolean.TRUE : Boolean.FALSE);
        return this;
    }

    /**
     * Put a value in the JSONArray, where the value will be a JSONArray which
     * is produced from a Collection.
     *
     * @param index The subscript.
     * @param value A Collection value.
     * @return this.
     * @throws JSONException If the index is negative or if the value is not
     *             finite.
     */
    public JSONArray put(final int index, final Collection<?> value)
        throws JSONException
    {
        return put(index, (Object)(value instanceof JSONArray ? value
            : new JSONArray(value)));
    }

    /**
     * Put or replace a double value. If the index is greater than the length of
     * the JSONArray, then null elements will be added as necessary to pad it
     * out.
     *
     * @param index The subscript.
     * @param value A double value.
     * @return this.
     * @throws JSONException If the index is negative or if the value is not
     *             finite.
     */
    public JSONArray put(final int index, final double value)
        throws JSONException
    {
        this.put(index, new Double(value));
        return this;
    }

    /**
     * Put or replace an int value. If the index is greater than the length of
     * the JSONArray, then null elements will be added as necessary to pad it
     * out.
     *
     * @param index The subscript.
     * @param value An int value.
     * @return this.
     * @throws JSONException If the index is negative.
     */
    public JSONArray put(final int index, final int value)
        throws JSONException
    {
        this.put(index, Integer.valueOf(value));
        return this;
    }

    /**
     * Put or replace a long value. If the index is greater than the length of
     * the JSONArray, then null elements will be added as necessary to pad it
     * out.
     *
     * @param index The subscript.
     * @param value A long value.
     * @return this.
     * @throws JSONException If the index is negative.
     */
    public JSONArray put(final int index, final long value)
        throws JSONException
    {
        this.put(index, Long.valueOf(value));
        return this;
    }

    /**
     * Put a value in the JSONArray, where the value will be a JSONObject that
     * is produced from a Map.
     *
     * @param index The subscript.
     * @param value The Map value.
     * @return this.
     * @throws JSONException If the index is negative or if the the value is an
     *             invalid number.
     */
    public JSONArray put(final int index, final Map<?, ?> value)
        throws JSONException
    {
        return put(index, (Object)(value instanceof JSONObject ? value
            : new JSONObject(value)));
    }

    /**
     * Put or replace an object value in the JSONArray. If the index is greater
     * than the length of the JSONArray, then null elements will be added as
     * necessary to pad it out.
     *
     * @param index The subscript.
     * @param value The value to put into the array. The value should be a
     *            Boolean, Double, Integer, JSONArray, JSONObject, Long, or
     *            String, or the JSONObject.NULL object.
     * @return this.
     * @throws JSONException If the index is negative or if the the value is an
     *             invalid number.
     */
    public JSONArray put(final int index, final Object value)
        throws JSONException
    {
        JSONObject.testValidity(value);
        if (index < 0)
            throw new JSONException("JSONArray[" + index + "] not found.");
        if (index < length())
            super.set(index, value);
        else {
            while (index != length())
                this.put(JSONObject.NULL);
            this.put(value);
        }
        return this;
    }

    /**
     * Remove an index and close the hole.
     *
     * @param index The index of the element to be removed.
     * @return The value that was associated with the index, or null if there
     *         was no value.
     */
    @Override
    public Object remove(final int index) {
        return index >= 0 && index < length() ? super.remove(index) : null;
    }

    /**
     * Determine if two JSONArrays are similar. They must contain similar
     * sequences.
     *
     * @param other The other JSONArray
     * @return true if they are equal
     */
    public boolean similar(final Object other) {
        if (!(other instanceof JSONArray))
            return false;
        final int len = length();
        if (len != ((JSONArray)other).length())
            return false;
        for (int i = 0; i < len; i += 1) {
            final Object valueThis = get(i);
            final Object valueOther = ((JSONArray)other).get(i);
            if (valueThis instanceof JSONObject) {
                if (!((JSONObject)valueThis).similar(valueOther))
                    return false;
            }
            else if (valueThis instanceof JSONArray) {
                if (!((JSONArray)valueThis).similar(valueOther))
                    return false;
            }
            else if (!valueThis.equals(valueOther))
                return false;
        }
        return true;
    }

    /**
     * Produce a JSONObject by combining a JSONArray of names with the values of
     * this JSONArray.
     *
     * @param names A JSONArray containing a list of key strings. These will be
     *            paired with the values.
     * @return A JSONObject, or null if there are no names or if this JSONArray
     *         has no values.
     * @throws JSONException If any of the names are null.
     */
    public JSONObject toJSONObject(final JSONArray names) throws JSONException {
        if (names == null || names.length() == 0 || length() == 0)
            return null;
        final JSONObject jo = new JSONObject();
        for (int i = 0; i < names.length(); i += 1)
            jo.put(names.getString(i), opt(i));
        return jo;
    }

    /**
     * Make a JSON text of this JSONArray. For compactness, no unnecessary
     * whitespace is added. If it is not possible to produce a syntactically
     * correct JSON text then null will be returned instead. This could occur if
     * the array contains an invalid number.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @return a printable, displayable, transmittable representation of the
     *         array.
     */
    @Override
    public String toString() {
        try {
            return this.toString(0);
        } catch (final Exception e) {
            return null;
        }
    }

    /**
     * Make a prettyprinted JSON text of this JSONArray. Warning: This method
     * assumes that the data structure is acyclical.
     *
     * @param indentFactor The number of spaces to add to each level of
     *            indentation.
     * @return a printable, displayable, transmittable representation of the
     *         object, beginning with <code>[</code>&nbsp;<small>(left
     *         bracket)</small> and ending with <code>]</code> &nbsp;
     *         <small>(right bracket)</small>.
     * @throws JSONException If something goes wrong
     */
    public String toString(final int indentFactor) throws JSONException {
        final StringWriter sw = new StringWriter();
        synchronized (sw.getBuffer()) {
            return this.write(sw, indentFactor, 0).toString();
        }
    }

    /**
     * Write the contents of the JSONArray as JSON text to a writer. For
     * compactness, no whitespace is added.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     * 
     * @param writer The writer
     * @return The writer.
     * @throws JSONException If something goes wrong
     */
    public Writer write(final Writer writer) throws JSONException {
        return this.write(writer, 0, 0);
    }

    /**
     * Write the contents of the JSONArray as JSON text to a writer. For
     * compactness, no whitespace is added.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @param writer Writes the serialized JSON
     * @param indentFactor The number of spaces to add to each level of
     *            indentation.
     * @param indent The indention of the top level.
     * @return The writer.
     * @throws JSONException If something goes wrong
     */
    public Writer write(final Writer writer, final int indentFactor,
        final int indent) throws JSONException
    {
        try {
            boolean commanate = false;
            final int length = length();
            writer.write('[');

            if (length == 1)
                JSONObject.writeValue(writer, super.get(0), indentFactor,
                    indent);
            else if (length != 0) {
                final int newindent = indent + indentFactor;

                for (int i = 0; i < length; i += 1) {
                    if (commanate)
                        writer.write(',');
                    if (indentFactor > 0)
                        writer.write('\n');
                    JSONObject.indent(writer, newindent);
                    JSONObject.writeValue(writer, super.get(i), indentFactor,
                        newindent);
                    commanate = true;
                }
                if (indentFactor > 0)
                    writer.write('\n');
                JSONObject.indent(writer, indent);
            }
            writer.write(']');
            return writer;
        } catch (final IOException e) {
            throw new JSONException(e);
        }
    }
}
