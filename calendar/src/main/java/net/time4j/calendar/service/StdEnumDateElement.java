/*
 * -----------------------------------------------------------------------
 * Copyright © 2013-2015 Meno Hochschild, <http://www.menodata.de/>
 * -----------------------------------------------------------------------
 * This file (StdEnumDateElement.java) is part of project Time4J.
 *
 * Time4J is free software: You can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * Time4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Time4J. If not, see <http://www.gnu.org/licenses/>.
 * -----------------------------------------------------------------------
 */

package net.time4j.calendar.service;

import net.time4j.engine.AttributeQuery;
import net.time4j.engine.ChronoDisplay;
import net.time4j.engine.ChronoEntity;
import net.time4j.engine.ChronoOperator;
import net.time4j.format.Attributes;
import net.time4j.format.CalendarText;
import net.time4j.format.NumericalElement;
import net.time4j.format.OutputContext;
import net.time4j.format.TextAccessor;
import net.time4j.format.TextElement;
import net.time4j.format.TextWidth;

import java.io.IOException;
import java.text.ParsePosition;
import java.util.Locale;

import static net.time4j.format.CalendarText.ISO_CALENDAR_TYPE;


/**
 * <p>General enum-based date element. </p>
 *
 * @author  Meno Hochschild
 * @since   3.5/4.3
 */
/*[deutsch]
 * <p>Allgemeines Enum-basiertes Datumselement. </p>
 *
 * @author  Meno Hochschild
 * @since   3.5/4.3
 */
public class StdEnumDateElement<V extends Enum<V>, T extends ChronoEntity<T>>
    extends StdDateElement<V, T>
    implements NumericalElement<V>, TextElement<V> {

    //~ Statische Felder/Initialisierungen --------------------------------

    private static final long serialVersionUID = -2452569351302286113L;

    //~ Instanzvariablen --------------------------------------------------

    private transient final Class<V> type;
    private transient final ChronoOperator<T> decrementor;
    private transient final ChronoOperator<T> incrementor;

    //~ Konstruktoren -----------------------------------------------------

    public StdEnumDateElement(
        String name,
        Class<T> chrono,
        Class<V> type,
        char symbol
    ) {
        super(name, chrono, symbol, isWeekdayElement(symbol));

        this.type = type;
        this.decrementor = null;
        this.incrementor = null;

    }

    public StdEnumDateElement(
        String name,
        Class<T> chrono,
        Class<V> type,
        char symbol,
        ChronoOperator<T> decrementor,
        ChronoOperator<T> incrementor
    ) {
        super(name, chrono, symbol, false);

        this.type = type;
        this.decrementor = decrementor;
        this.incrementor = incrementor;

    }

    //~ Methoden ----------------------------------------------------------

    @Override
    public Class<V> getType() {

        return this.type;

    }

    @Override
    public V getDefaultMinimum() {

        return this.type.getEnumConstants()[0];

    }

    @Override
    public V getDefaultMaximum() {

        V[] enums = this.type.getEnumConstants();
        return enums[enums.length - 1];

    }

    @Override
    public int numerical(V value) {

        return value.ordinal() + 1;

    }

    @Override
    public ChronoOperator<T> decremented() {

        if (this.decrementor != null) {
            return this.decrementor;
        }

        return super.decremented();

    }

    @Override
    public ChronoOperator<T> incremented() {

        if (this.incrementor != null) {
            return this.incrementor;
        }

        return super.incremented();

    }

    @Override
    public void print(
        ChronoDisplay context,
        Appendable buffer,
        AttributeQuery attributes
    ) throws IOException {

        V value = context.get(this);
        buffer.append(this.accessor(attributes, isLeapMonth(value)).print(value));

    }

    @Override
    public V parse(
        CharSequence text,
        ParsePosition status,
        AttributeQuery attributes
    ) {

        int index = status.getIndex();
        V result = this.accessor(attributes, false).parse(text, status, this.getType(), attributes);

        if (
            this.isMonthElement()
            && (status.getErrorIndex() != -1)
        ) {
            status.setErrorIndex(-1);
            status.setIndex(index);
            result = this.accessor(attributes, true).parse(text, status, this.getType(), attributes);
        }

        return result;

    }

    /**
     * <p>Does given element value represent a leap form? </p>
     *
     * <p>Example: If given value is the hebrew month ADAR-I or ADAR-II
     * then this method must return {@code true}. </p>
     *
     * @param   value   element value
     * @return  {@code false} by default
     * @since   3.5/4.3
     */
    protected boolean isLeapMonth(V value) {

        return false;

    }

    /**
     * <p>Does this element represent a calendar era? </p>
     *
     * <p>The default implementation returns {@code true}
     * if and only if the associated format pattern symbol is G. </p>
     *
     * @return  boolean
     * @since   3.5/4.3
     */
    protected boolean isEraElement() {

        return (this.getSymbol() == 'G');

    }

    /**
     * <p>Does this element represent a calendar month? </p>
     *
     * <p>The default implementation returns {@code true}
     * if and only if the associated format pattern symbol is M. </p>
     *
     * @return  boolean
     * @since   3.5/4.3
     */
    protected boolean isMonthElement() {

        return (this.getSymbol() == 'M');

    }

    /**
     * <p>Does this element represent a day of any calendar week? </p>
     *
     * <p>The default implementation returns {@code true}
     * if and only if the associated format pattern symbol is E. </p>
     *
     * @return  boolean
     * @since   3.5/4.3
     */
    protected boolean isWeekdayElement() {

        return isWeekdayElement(this.getSymbol());

    }

    private static boolean isWeekdayElement(char symbol) {

        return (symbol == 'E');

    }

    private TextAccessor accessor(
        AttributeQuery attributes,
        boolean leap
    ) {

        Locale lang = attributes.get(Attributes.LANGUAGE, Locale.ROOT);
        TextWidth textWidth = attributes.get(Attributes.TEXT_WIDTH, TextWidth.WIDE);
        OutputContext outputContext = attributes.get(Attributes.OUTPUT_CONTEXT, OutputContext.FORMAT);

        CalendarText cnames;

        if (this.isMonthElement()) {
            cnames =
                CalendarText.getInstance(
                    attributes.get(Attributes.CALENDAR_TYPE, ISO_CALENDAR_TYPE),
                    lang);
            if (leap) {
                return cnames.getLeapMonths(textWidth, outputContext);
            } else {
                return cnames.getStdMonths(textWidth, outputContext);
            }
        } else if (this.isWeekdayElement()) {
            cnames = CalendarText.getInstance(ISO_CALENDAR_TYPE, lang);
            return cnames.getWeekdays(textWidth, outputContext);
        } else if (this.isEraElement()) {
            cnames =
                CalendarText.getInstance(
                    attributes.get(Attributes.CALENDAR_TYPE, ISO_CALENDAR_TYPE),
                    lang);
            return cnames.getEras(textWidth);
        } else {
            throw new UnsupportedOperationException(this.name());
        }

    }

}