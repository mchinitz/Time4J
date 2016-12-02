/*
 * -----------------------------------------------------------------------
 * Copyright © 2013-2016 Meno Hochschild, <http://www.menodata.de/>
 * -----------------------------------------------------------------------
 * This file (SimpleInterval.java) is part of project Time4J.
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

package net.time4j.range;

import net.time4j.engine.AttributeQuery;
import net.time4j.engine.ChronoDisplay;
import net.time4j.engine.ChronoFunction;
import net.time4j.engine.TimeLine;
import net.time4j.format.FormatPatternProvider;
import net.time4j.format.expert.ChronoParser;
import net.time4j.format.expert.ChronoPrinter;
import net.time4j.format.expert.ParseLog;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;


/**
 * <p>Generic interval class suitable for any type of timepoints on a timeline. </p>
 *
 * <p>Represents an interval with following simplified features: </p>
 *
 * <ul>
 *     <li>Always half-open with inclusive start and exclusive end. </li>
 *     <li>Supports infinite boundaries (the only exception to half-open-state). </li>
 *     <li>Can be adapted to any foreign type as long as a timeline can be implemented. </li>
 *     <li>Can be used in conjunction with {@code IntervalCollection} and {@code IntervalTree}. </li>
 * </ul>
 *
 * <p>This class is mainly intended to adapt foreign types like {@code java.util.Date}.
 * It is less suitable for calendrical types. </p>
 *
 * @param   <T> generic type of timepoints on the underlying timeline
 * @author  Meno Hochschild
 * @since   3.25/4.21
 */
/*[deutsch]
 * <p>Allgemeine Intervallklasse geeignet f&uuml;r generische Zeitpunkte auf einem beliebigen Zeitstrahl. </p>
 *
 * <p>Repr&auml;sentiert ein Intervall mit folgenden vereinfachten Eigenschaften: </p>
 *
 * <ul>
 *     <li>Immer halb-offen mit dem Start inklusive und dem Ende exklusive. </li>
 *     <li>Unterst&uuml;tzt unendliche Grenzen (die einzige Ausnahme vom halb-offenen Zustand). </li>
 *     <li>Kann an irgendeinen Fremdtyp angepasst werden, solange ein Zeitstrahl passend konstruiert wird. </li>
 *     <li>Kann in Verbindung mit {@code IntervalCollection} und {@code IntervalTree} genutzt werden. </li>
 * </ul>
 *
 * <p>Diese Klasse dient haupts&auml;chlich zur Verwendung mit Fremdtypen wie {@code java.util.Date}.
 * Sie ist f&uuml;r rein kalendarische Typen weniger geeignet. </p>
 *
 * @param   <T> generic type of timepoints on the underlying timeline
 * @author  Meno Hochschild
 * @since   3.25/4.21
 */
public final class SimpleInterval<T>
    implements ChronoInterval<T>, Serializable {

    //~ Statische Felder/Initialisierungen --------------------------------

    private static final ChronoFunction<ChronoDisplay, Void> NO_RESULT =
        new ChronoFunction<ChronoDisplay, Void>() {
            @Override
            public Void apply(ChronoDisplay context) {
                return null;
            }
        };

    private static final Factory<Date> OLD_DATE_FACTORY = new Factory<Date>(new TraditionalTimeLine());

    private static final long serialVersionUID = -3508139527445140226L;

    //~ Instanzvariablen --------------------------------------------------

    /**
     * @serial  start boundary
     */
    private final Boundary<T> start;

    /**
     * @serial  end boundary
     */
    private final Boundary<T> end;

    /**
     * @serial  the underlying timeline
     */
    private final TimeLine<T> timeLine;

    //~ Konstruktoren -----------------------------------------------------

    SimpleInterval(
        T start,
        T end,
        TimeLine<T> timeLine
    ) {
        super();

        if (timeLine == null) {
            throw new NullPointerException();
        }

        if ((start != null) && (end != null) && timeLine.compare(start, end) > 0) {
            throw new IllegalArgumentException("Start after end: " + start + "/" + end);
        }

        this.start = ((start == null) ? Boundary.<T>infinitePast() : Boundary.ofClosed(start));
        this.end = ((end == null) ? Boundary.<T>infiniteFuture() : Boundary.ofOpen(end));
        this.timeLine = timeLine;

    }

    SimpleInterval(
        Boundary<T> start,
        Boundary<T> end,
        TimeLine<T> timeLine
    ) {
        super();

        this.start = start;
        this.end = end;
        this.timeLine = timeLine;

    }

    //~ Methoden ----------------------------------------------------------

    /**
     * <p>Creates a new interval between given boundaries. </p>
     *
     * @param   start   the start of interval (inclusive)
     * @param   end     the end of interval (exclusive)
     * @return  new interval (half-open)
     */
    /*[deutsch]
     * <p>Erzeugt ein neues Intervall mit den angegebenen Grenzen. </p>
     *
     * @param   start   the start of interval (inclusive)
     * @param   end     the end of interval (exclusive)
     * @return  new interval (half-open)
     */
    public static SimpleInterval<Date> between(
        Date start,
        Date end
    ) {

        return OLD_DATE_FACTORY.between(start, end);

    }

    /**
     * <p>Creates a new interval since given start. </p>
     *
     * @param   start   the start of interval (inclusive)
     * @return  new interval (half-open and infinite)
     */
    /*[deutsch]
     * <p>Erzeugt ein neues Intervall seit dem angegebenen Start. </p>
     *
     * @param   start   the start of interval (inclusive)
     * @return  new interval (half-open and infinite)
     */
    public static SimpleInterval<Date> since(Date start) {

        return OLD_DATE_FACTORY.since(start);

    }

    /**
     * <p>Creates a new interval until given end. </p>
     *
     * @param   end     the end of interval (exclusive)
     * @return  new interval (open and infinite)
     */
    /*[deutsch]
     * <p>Erzeugt ein neues Intervall bis zum angegebenen Ende. </p>
     *
     * @param   end     the end of interval (exclusive)
     * @return  new interval (open and infinite)
     */
    public static SimpleInterval<Date> until(Date end) {

        return OLD_DATE_FACTORY.until(end);

    }

    /**
     * <p>Defines a timeline on which new intervals for the type {@code java.util.Date} can be created. </p>
     *
     * @return  singleton interval factory
     */
    /*[deutsch]
     * <p>Definiert einen Zeitstrahl, auf dem neue Intervalle f&uuml;r den Typ {@code java.util.Date}
     * erzeugt werden k&ouml;nnen. </p>
     *
     * @return  singleton interval factory
     */
    public static Factory<Date> onTraditionalTimeLine() {

        return OLD_DATE_FACTORY;

    }

    /**
     * <p>Defines a timeline on which new generic intervals can be created. </p>
     *
     * <p>Note that the given timeline must be serializable to make the produced simple intervals
     * serializable, too. </p>
     *
     * @param   <T> generic type of timepoints on the underlying timeline
     * @param   timeLine    the timeline definition
     * @return  new interval factory
     */
    /*[deutsch]
     * <p>Definiert einen Zeitstrahl, auf dem neue generische Intervalle erzeugt werden k&ouml;nnen. </p>
     *
     * <p>Hinweis: Der angegebene Zeitstrahl mu&szlig; serialisierbar sein, damit alle darauf erzeugten
     * Intervalle serialisierbar sind. </p>
     *
     * @param   <T> generic type of timepoints on the underlying timeline
     * @param   timeLine    the timeline definition
     * @return  new interval factory
     */
    public static <T> Factory<T> onTimeLine(TimeLine<T> timeLine) {

        return new Factory<T>(timeLine);

    }

    @Override
    public Boundary<T> getStart() {

        return this.start;

    }

    @Override
    public Boundary<T> getEnd() {

        return this.end;

    }

    @Override
    public boolean isFinite() {

        return !(this.getStart().isInfinite() || this.getEnd().isInfinite());

    }

    @Override
    public boolean isEmpty() {

        if (!this.isFinite()) {
            return false;
        }

        return (this.timeLine.compare(this.start.getTemporal(), this.end.getTemporal()) == 0);

    }

    @Override
    public boolean contains(T temporal) {

        if (temporal == null) {
            throw new NullPointerException();
        } else if (!this.start.isInfinite() && (this.timeLine.compare(temporal, this.start.getTemporal()) < 0)) {
            return false;
        }

        if (!this.end.isInfinite()) {
            return (this.timeLine.compare(this.end.getTemporal(), temporal) > 0);
        }

        return true;

    }

    @Override
    public boolean contains(ChronoInterval<T> other) {

        if (!other.isFinite()) {
            return false;
        }

        T startA = this.start.getTemporal();
        T startB = other.getStart().getTemporal();

        if (other.getStart().isOpen()) {
            startB = this.timeLine.stepForward(startB);
        }

        if ((startB == null) || ((startA != null) && (this.timeLine.compare(startA, startB) > 0))) {
            return false;
        }

        T endA = this.end.getTemporal();

        if (endA == null) {
            return true;
        }

        T endB = other.getEnd().getTemporal();

        if (
            other.getEnd().isOpen()
            && (this.timeLine.compare(startB, endB) == 0)
        ) {
            if ((endA == null) || (this.timeLine.compare(startB, endA) >= 0)) {
                return false;
            }
        } else {
            if (other.getEnd().isClosed()) {
                endB = this.timeLine.stepForward(endB);
                if (endB == null) {
                    return false;
                }
            }
            return (this.timeLine.compare(endA, endB) >= 0);
        }

        return true;

    }

    @Override
    public boolean isAfter(T temporal) {

        if (temporal == null) {
            throw new NullPointerException();
        } else if (this.start.isInfinite()) {
            return false;
        }

        return (this.timeLine.compare(this.start.getTemporal(), temporal) > 0);

    }

    @Override
    public boolean isAfter(ChronoInterval<T> other) {

        return other.isBefore(this);

    }

    @Override
    public boolean isBefore(T temporal) {

        if (temporal == null) {
            throw new NullPointerException();
        } else if (this.end.isInfinite()) {
            return false;
        }

        return (this.timeLine.compare(this.end.getTemporal(), temporal) <= 0);

    }

    @Override
    public boolean isBefore(ChronoInterval<T> other) {

        if (other.getStart().isInfinite() || this.end.isInfinite()) {
            return false;
        }

        T endA = this.end.getTemporal();
        T startB = other.getStart().getTemporal();

        if (other.getStart().isOpen()) {
            startB = this.timeLine.stepForward(startB);
        }

        if (startB == null) { // exotic case: start in infinite future
            return true;
        } else {
            return (this.timeLine.compare(endA, startB) <= 0);
        }

    }

    @Override
    public boolean abuts(ChronoInterval<T> other) {

        if (this.isEmpty() || other.isEmpty()) {
            return false;
        }

        T startA = this.start.getTemporal();
        T startB = other.getStart().getTemporal();

        if ((startB != null) && other.getStart().isOpen()) {
            startB = this.timeLine.stepForward(startB);
        }

        T endA = this.end.getTemporal();
        T endB = other.getEnd().getTemporal();

        if ((endB != null) && other.getEnd().isClosed()) {
            endB = this.timeLine.stepForward(endB);
        }

        if ((endA == null) || (startB == null)) {
            return ((startA != null) && (endB != null) && (this.timeLine.compare(startA, endB) == 0));
        } else if ((startA == null) || (endB == null)) {
            return (this.timeLine.compare(endA, startB) == 0);
        }

        return (this.timeLine.compare(endA, startB) == 0) ^ (this.timeLine.compare(startA, endB) == 0);

    }

    @Override
    public boolean intersects(ChronoInterval<T> other) {

        if (this.isEmpty() || other.isEmpty()) {
            return false;
        }

        return !(this.isBefore(other) || this.isAfter(other));

    }

    /**
     * <p>Obtains the intersection of this interval and other one if present. </p>
     *
     * <p>Note that starting with v4.21 the return type has changed to
     * {@code Optional&lt;SimpleInterval&lt;T&gt;&gt;}. </p>
     *
     * @param   other   another interval which might have an intersection with this interval
     * @return  found intersection or {@code null}
     * @see     #intersects(ChronoInterval)
     */
    /*[deutsch]
     * <p>Ermittelt die Schnittmenge dieses Intervalls mit dem angegebenen anderen Intervall, falls vorhanden. </p>
     *
     * <p>Zu beachten: Beginnend mit v4.21 &auml;ndert sich der return-type zu
     * {@code Optional&lt;SimpleInterval&lt;T&gt;&gt;}. </p>
     *
     * @param   other   another interval which might have an intersection with this interval
     * @return  found intersection or {@code null}
     * @see     #intersects(ChronoInterval)
     */
    public SimpleInterval<T> findIntersection(ChronoInterval<T> other) {

        if (this.isEmpty() || other.isEmpty()) {
            return null;
        }

        Boundary<T> s;
        Boundary<T> e;

        if (this.start.isInfinite()) {
            s = other.getStart();
        } else if (other.getStart().isInfinite()) {
            s = this.start;
        } else {
            T t1 = this.start.getTemporal();
            T t2 = other.getStart().getTemporal();
            if (other.getStart().isOpen()) {
                t2 = this.timeLine.stepForward(t2);
            }
            if ((t1 == null) || (t2 == null)) {
                return null;
            }
            s = ((this.timeLine.compare(t1, t2) < 0) ? Boundary.ofClosed(t2) : Boundary.ofClosed(t1));
        }

        if (this.end.isInfinite()) {
            e = other.getEnd();
        } else if (other.getEnd().isInfinite()) {
            e = this.end;
        } else {
            T t1 = this.end.getTemporal();
            T t2 = other.getEnd().getTemporal();
            if (other.getEnd().isClosed()) {
                t2 = this.timeLine.stepForward(t2);
            }
            if (t1 == null) {
                e = ((t2 == null) ? Boundary.<T>infiniteFuture() : Boundary.ofOpen(t2));
            } else if (t2 == null) {
                e = ((t1 == null) ? Boundary.<T>infiniteFuture() : Boundary.ofOpen(t1));
            } else {
                e = ((this.timeLine.compare(t1, t2) < 0) ? Boundary.ofOpen(t1) : Boundary.ofOpen(t2));
            }
        }

        if (isAfter(s, e)) {
            return null;
        } else {
            SimpleInterval<T> intersection = new SimpleInterval<T>(s, e, this.timeLine);
            return (intersection.isEmpty() ? null : intersection);
        }

    }

    /**
     * <p>Prints this interval using a localized interval pattern. </p>
     *
     * <p>If given printer does not contain a reference to a locale then the interval pattern
     * &quot;{0}/{1}&quot; will be used. </p>
     *
     * @param   printer     format object for printing start and end
     * @return  localized formatted string
     * @see     #print(ChronoPrinter, String)
     * @see     FormatPatternProvider#getIntervalPattern(Locale)
     */
    /*[deutsch]
     * <p>Formatiert dieses Intervall mit Hilfe eines lokalisierten Intervallmusters. </p>
     *
     * <p>Falls der angegebene Formatierer keine Referenz zu einer Sprach- und L&auml;ndereinstellung hat, wird
     * das Intervallmuster &quot;{0}/{1}&quot; verwendet. </p>
     *
     * @param   printer     format object for printing start and end
     * @return  localized formatted string
     * @see     #print(ChronoPrinter, String)
     * @see     FormatPatternProvider#getIntervalPattern(Locale)
     */
    public String print(ChronoPrinter<T> printer) {

        return this.print(printer, IsoInterval.getIntervalPattern(printer));

    }

    /**
     * <p>Prints this interval in a custom format. </p>
     *
     * @param   printer             format object for printing start and end components
     * @param   intervalPattern     interval pattern containing placeholders {0} and {1} (for start and end)
     * @return  formatted string in given pattern format
     */
    /*[deutsch]
     * <p>Formatiert dieses Intervall in einem benutzerdefinierten Format. </p>
     *
     * @param   printer             format object for printing start and end components
     * @param   intervalPattern     interval pattern containing placeholders {0} and {1} (for start and end)
     * @return  formatted string in given pattern format
     */
    public String print(
        ChronoPrinter<T> printer,
        String intervalPattern
    ) {

        AttributeQuery attrs = IsoInterval.extractDefaultAttributes(printer);
        StringBuilder sb = new StringBuilder(32);
        int i = 0;
        int n = intervalPattern.length();

        try {
            while (i < n) {
                char c = intervalPattern.charAt(i);
                if ((c == '{') && (i + 2 < n) && (intervalPattern.charAt(i + 2) == '}')) {
                    char next = intervalPattern.charAt(i + 1);
                    if (next == '0') {
                        if (this.start.isInfinite()) {
                            sb.append("-\u221E");
                        } else {
                            printer.print(this.start.getTemporal(), sb, attrs, NO_RESULT);
                        }
                        i += 3;
                        continue;
                    } else if (next == '1') {
                        if (this.end.isInfinite()) {
                            sb.append("+\u221E");
                        } else {
                            printer.print(this.end.getTemporal(), sb, attrs, NO_RESULT);
                        }
                        i += 3;
                        continue;
                    }
                }
                sb.append(c);
                i++;
            }
        } catch (IOException ioe) {
            throw new AssertionError(ioe);
        }

        return sb.toString();

    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        } else if (obj instanceof SimpleInterval) {
            SimpleInterval<?> that = (SimpleInterval<?>) obj;
            return this.start.equals(that.start) && this.end.equals(that.end) && this.timeLine.equals(that.timeLine);
        } else {
            return false;
        }

    }

    @Override
    public int hashCode() {

        return this.start.hashCode() ^ this.end.hashCode();

    }

    /**
     * <p>Returns a string in technical notation (suitable for debugging purposes). </p>
     *
     * @return  String
     */
    /*[deutsch]
     * <p>Liefert eine technische Beschreibung, die vor allem zum Debugging geeignet ist. </p>
     *
     * @return  String
     */
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        if (this.start.isInfinite()) {
            sb.append("(-\u221E");
        } else {
            sb.append('[');
            sb.append(this.start.getTemporal());
        }
        sb.append('/');
        if (this.end.isInfinite()) {
            sb.append("+\u221E)");
        } else {
            sb.append(this.end.getTemporal());
            sb.append(')');
        }
        return sb.toString();

    }

    private boolean isAfter(
        Boundary<T> start,
        Boundary<T> end
    ) {

        if (start.isInfinite()|| end.isInfinite()) {
            return false;
        } else {
            return (this.timeLine.compare(start.getTemporal(), end.getTemporal()) > 0);
        }

    }

    //~ Innere Klassen ----------------------------------------------------

    /**
     * <p>Serves for the creation of generic simple intervals on a timeline. </p>
     *
     * @param   <T> generic type of timepoints on the underlying timeline
     */
    /*[deutsch]
     * <p>Dient der Erzeugung von allgemeinen einfachen Intervallen auf einem Zeitstrahl. </p>
     *
     * @param   <T> generic type of timepoints on the underlying timeline
     */
    public static class Factory<T> {

        //~ Instanzvariablen ----------------------------------------------

        private final TimeLine<T> timeLine;

        //~ Konstruktoren -------------------------------------------------

        private Factory(TimeLine<T> timeLine) {
            super();

            if (timeLine == null) {
                throw new NullPointerException("Missing timeline.");
            }

            this.timeLine = timeLine;

        }

        //~ Methoden ------------------------------------------------------

        /**
         * <p>Creates a new interval between given boundaries. </p>
         *
         * @param   start   the start of interval (inclusive)
         * @param   end     the end of interval (exclusive)
         * @return  new interval (half-open)
         */
        /*[deutsch]
         * <p>Erzeugt ein neues Intervall mit den angegebenen Grenzen. </p>
         *
         * @param   start   the start of interval (inclusive)
         * @param   end     the end of interval (exclusive)
         * @return  new interval (half-open)
         */
        public SimpleInterval<T> between(
            T start,
            T end
        ) {

            if (start == null) {
                throw new NullPointerException("Missing start.");
            } else if (end == null) {
                throw new NullPointerException("Missing end.");
            }

            return new SimpleInterval<T>(start, end, this.timeLine);

        }

        /**
         * <p>Creates a new interval since given start. </p>
         *
         * @param   start   the start of interval (inclusive)
         * @return  new interval (half-open and infinite)
         */
        /*[deutsch]
         * <p>Erzeugt ein neues Intervall seit dem angegebenen Start. </p>
         *
         * @param   start   the start of interval (inclusive)
         * @return  new interval (half-open and infinite)
         */
        public SimpleInterval<T> since(T start) {

            if (start == null) {
                throw new NullPointerException("Missing start.");
            }

            return new SimpleInterval<T>(start, null, this.timeLine);

        }

        /**
         * <p>Creates a new interval until given end. </p>
         *
         * @param   end     the end of interval (exclusive)
         * @return  new interval (open and infinite)
         */
        /*[deutsch]
         * <p>Erzeugt ein neues Intervall bis zum angegebenen Ende. </p>
         *
         * @param   end     the end of interval (exclusive)
         * @return  new interval (open and infinite)
         */
        public SimpleInterval<T> until(T end) {

            if (end == null) {
                throw new NullPointerException("Missing end.");
            }

            return new SimpleInterval<T>(null, end, this.timeLine);

        }

        /**
         * <p>Interpretes given text as interval using a localized interval pattern. </p>
         *
         * <p>If given parser does not contain a reference to a locale then the interval pattern
         * &quot;{0}/{1}&quot; will be used. </p>
         *
         * @param   text        text to be parsed
         * @param   parser      format object for parsing start and end components
         * @return  parsed interval
         * @throws  IndexOutOfBoundsException if given text is empty
         * @throws  ParseException if the text is not parseable
         * @see     #parse(CharSequence, ChronoParser, String)
         */
        /*[deutsch]
         * <p>Interpretiert den angegebenen Text als Intervall mit Hilfe eines lokalisierten
         * Intervallmusters. </p>
         *
         * <p>Falls der angegebene Formatierer keine Referenz zu einer Sprach- und L&auml;ndereinstellung hat, wird
         * das Intervallmuster &quot;{0}/{1}&quot; verwendet. </p>
         *
         * @param   text        text to be parsed
         * @param   parser      format object for parsing start and end components
         * @return  parsed interval
         * @throws  IndexOutOfBoundsException if given text is empty
         * @throws  ParseException if the text is not parseable
         * @see     #parse(CharSequence, ChronoParser, String)
         */
        public SimpleInterval<T> parse(
            CharSequence text,
            ChronoParser<T> parser
        ) throws ParseException {

            return parse(text, parser, IsoInterval.getIntervalPattern(parser));

        }

        /**
         * <p>Interpretes given text as interval using given interval pattern. </p>
         *
         * <p>For version v4.21 or later, it is also possible to use an or-pattern logic. Example
         * see {@link DateInterval#parse(String, ChronoParser, String)}. </p>
         *
         * @param   text                text to be parsed
         * @param   parser              format object for parsing start and end components
         * @param   intervalPattern     interval pattern containing placeholders {0} and {1} (for start and end)
         * @return  parsed interval
         * @throws  IndexOutOfBoundsException if given text is empty
         * @throws  ParseException if the text is not parseable
         */
        /*[deutsch]
         * <p>Interpretiert den angegebenen Text als Intervall mit Hilfe des angegebenen
         * Intervallmusters. </p>
         *
         * <p>F&uuml;r die Version v4.21 oder sp&auml;ter ist es auch m&ouml;glich, eine Oder-Logik im Muster
         * zu verwenden. Beispiel siehe {@link DateInterval#parse(String, ChronoParser, String)}. </p>
         *
         * @param   text                text to be parsed
         * @param   parser              format object for parsing start and end components
         * @param   intervalPattern     interval pattern containing placeholders {0} and {1} (for start and end)
         * @return  parsed interval
         * @throws  IndexOutOfBoundsException if given text is empty
         * @throws  ParseException if the text is not parseable
         */
        public SimpleInterval<T> parse(
            CharSequence text,
            ChronoParser<T> parser,
            String intervalPattern
        ) throws ParseException {

            IntervalCreator<T, SimpleInterval<T>> icreator =
                new IntervalCreator<T, SimpleInterval<T>>() {
                    @Override
                    public SimpleInterval<T> between(Boundary<T> start, Boundary<T> end) {
                        return new SimpleInterval<T>(start, end, timeLine);
                    }
                    @Override
                    public boolean isCalendrical() {
                        return false;
                    }
                };
            ParseLog plog = new ParseLog();

            SimpleInterval<T> interval =
                IntervalParser.parseCustom(text, icreator, parser, intervalPattern, plog);

            if (plog.isError()) {
                throw new ParseException(plog.getErrorMessage(), plog.getErrorIndex());
            } else if (interval == null) {
                throw new ParseException("Parsing of interval failed: " + text, plog.getPosition());
            }

            return interval;

        }

        // package-private
        TimeLine<T> getTimeLine() {

            return this.timeLine;

        }

    }

    private static class TraditionalTimeLine
        implements TimeLine<Date>, Serializable {

        //~ Methoden ------------------------------------------------------

        @Override
        public Date stepForward(Date timepoint) {
            if (timepoint.getTime() == Long.MAX_VALUE) {
                return null;
            }
            return new Date(timepoint.getTime() + 1);
        }

        @Override
        public Date stepBackwards(Date timepoint) {
            if (timepoint.getTime() == Long.MIN_VALUE) {
                return null;
            }
            return new Date(timepoint.getTime() - 1);
        }

        @Override
        public int compare(Date o1, Date o2) {
            return o1.compareTo(o2);
        }

        private Object readResolve() throws ObjectStreamException {
            return OLD_DATE_FACTORY.getTimeLine();
        }

    }

}
