/*
 * -----------------------------------------------------------------------
 * Copyright © 2013-2014 Meno Hochschild, <http://www.menodata.de/>
 * -----------------------------------------------------------------------
 * This file (ChronoInterval.java) is part of project Time4J.
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

import net.time4j.Moment;
import net.time4j.PlainDate;
import net.time4j.PlainTime;
import net.time4j.PlainTimestamp;
import net.time4j.engine.ChronoEntity;
import net.time4j.engine.Temporal;
import net.time4j.engine.TimeLine;
import net.time4j.format.ChronoFormatter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * <p>Represents a temporal interval on a timeline. </p>
 *
 * @param   <T> temporal type of time points within a given interval
 * @author  Meno Hochschild
 * @since   1.3
 */
/**
 * <p>Repr&auml;sentiert ein Zeitintervall auf einem Zeitstrahl. </p>
 *
 * @param   <T> temporal type of time points within a given interval
 * @author  Meno Hochschild
 * @since   1.3
 */
public abstract class ChronoInterval
    <T extends ChronoEntity<T> & Temporal<? super T>> {

    //~ Statische Felder/Initialisierungen --------------------------------

    private static final Map<TimeLine<?>, IntervalFactory<?>> FACTORIES;

    static {
        Map<TimeLine<?>, IntervalFactory<?>> map =
            new HashMap<TimeLine<?>, IntervalFactory<?>>();
        map.put(PlainDate.axis(), DateIntervalFactory.INSTANCE);
        map.put(PlainTime.axis(), TimeIntervalFactory.INSTANCE);
        map.put(PlainTimestamp.axis(), TimestampIntervalFactory.INSTANCE);
        map.put(Moment.axis(), MomentIntervalFactory.INSTANCE);
        FACTORIES = Collections.unmodifiableMap(map);
    }

    //~ Instanzvariablen --------------------------------------------------

    private final Boundary<T> start;
    private final Boundary<T> end;

    //~ Konstruktoren -----------------------------------------------------

    /**
     * <p>Paket-privater Standardkonstruktor f&uuml;r Subklassen. </p>
     *
     * @param   start   untere Intervallgrenze
     * @param   end     obere Intervallgrenze
     * @throws  IllegalArgumentException if start is after end
     */
    ChronoInterval(
        Boundary<T> start,
        Boundary<T> end
    ) {
        super();


        if (start.isAfter(end)) { // NPE-check
            throw new IllegalArgumentException(
                "Start after end: " + start + "/" + end);
        } else if (
            end.isOpen() // NPE-check
            && start.isOpen()
            && start.isSimultaneous(end)
        ) {
            if (start.isInfinite()) {
                throw new IllegalArgumentException(
                    "Infinite boundaries must not be equal.");
            } else {
                throw new IllegalArgumentException(
                    "Open start after open end: " + start + "/" + end);
            }
        }

        this.start = start;
        this.end = end;

    }

    //~ Methoden ----------------------------------------------------------

    /**
     * <p>Determines a generic factory for finite intervals
     * on given timeline. </p>
     *
     * <p>User will usually use this method only for creating finite
     * intervals on specific non-basic timelines. </p>
     *
     * @param   <T> generic temporal type
     * @param   timeline    time axis
     * @return  generic interval factory which only supports finite intervals
     * @since   1.3
     * @see     DateInterval#between(PlainDate,PlainDate)
     * @see     TimeInterval#between(PlainTime,PlainTime)
     * @see     TimestampInterval#between(PlainTimestamp,PlainTimestamp)
     * @see     MomentInterval#between(Moment,Moment)
     */
    /*[deutsch]
     * <p>Bestimmt eine Fabrik f&uuml;r begrenzte Intervalle
     * auf dem angegebenen Zeitstrahl. </p>
     *
     * <p>Anwender werden diese Methode normalerweise nur f&uuml;r die
     * Erzeugung von begrenzten Intervallen auf einem speziellen
     * Nicht-Standard-Zeitstrahl verwenden. </p>
     *
     * @param   <T> generic temporal type
     * @param   timeline    time axis
     * @return  generic interval factory which only supports finite intervals
     * @since   1.3
     * @see     DateInterval#between(PlainDate,PlainDate)
     * @see     TimeInterval#between(PlainTime,PlainTime)
     * @see     TimestampInterval#between(PlainTimestamp,PlainTimestamp)
     * @see     MomentInterval#between(Moment,Moment)
     */
    @SuppressWarnings("unchecked")
    public static
    <T extends ChronoEntity<T> & Temporal<? super T>> IntervalFactory<T> on(
        TimeLine<T> timeline
    ) {

        IntervalFactory<?> factory = FACTORIES.get(timeline);

        if (factory == null) {
            return new GenericIntervalFactory<T>(timeline);
        } else {
            return (IntervalFactory<T>) factory;
        }

    }

    /**
     * <p>Yields the lower bound of this interval. </p>
     *
     * @return  start interval boundary
     * @since   1.3
     */
    /*[deutsch]
     * <p>Liefert die untere Grenze dieses Intervalls. </p>
     *
     * @return  start interval boundary
     * @since   1.3
     */
    public final Boundary<T> getStart() {

        return this.start;

    }

    /**
     * <p>Yields the upper bound of this interval. </p>
     *
     * @return  end interval boundary
     * @since   1.3
     */
    /*[deutsch]
     * <p>Liefert die obere Grenze dieses Intervalls. </p>
     *
     * @return  end interval boundary
     * @since   1.3
     */
    public final Boundary<T> getEnd() {

        return this.end;

    }

    /**
     * <p>Yields a copy of this interval with given start boundary. </p>
     *
     * @param   boundary    new start interval boundary
     * @return  possibly changed copy of this interval
     * @throws  IllegalArgumentException if given boundary is infinite and
     *          the concrete interval does not support infinite boundaries
     *          or if new start is after end
     * @since   1.3
     */
    /*[deutsch]
     * <p>Liefert eine Kopie dieses Intervalls mit der angegebenen unteren
     * Grenze. </p>
     *
     * @param   boundary    new start interval boundary
     * @return  possibly changed copy of this interval
     * @throws  IllegalArgumentException if given boundary is infinite and
     *          the concrete interval does not support infinite boundaries
     *          or if new start is after end
     * @since   1.3
     */
    public abstract ChronoInterval<T> withStart(Boundary<T> boundary);

    /**
     * <p>Yields a copy of this interval with given end boundary. </p>
     *
     * @param   boundary    new end interval boundary
     * @return  possibly changed copy of this interval
     * @throws  IllegalArgumentException if given boundary is infinite and
     *          the concrete interval does not support infinite boundaries
     *          or if new end is before start
     * @since   1.3
     */
    /*[deutsch]
     * <p>Liefert eine Kopie dieses Intervalls mit der angegebenen oberen
     * Grenze. </p>
     *
     * @param   boundary    new end interval boundary
     * @return  possibly changed copy of this interval
     * @throws  IllegalArgumentException if given boundary is infinite and
     *          the concrete interval does not support infinite boundaries
     *          or if new end is before start
     * @since   1.3
     */
    public abstract ChronoInterval<T> withEnd(Boundary<T> boundary);

    /**
     * <p>Yields a copy of this interval with given start time. </p>
     *
     * @param   temporal    new start timepoint
     * @return  possibly changed copy of this interval
     * @throws  IllegalArgumentException if new start is after end
     * @since   1.3
     */
    /*[deutsch]
     * <p>Liefert eine Kopie dieses Intervalls mit der angegebenen
     * Startzeit. </p>
     *
     * @param   temporal    new start timepoint
     * @return  possibly changed copy of this interval
     * @throws  IllegalArgumentException if new start is after end
     * @since   1.3
     */
    public abstract ChronoInterval<T> withStart(T temporal);

    /**
     * <p>Yields a copy of this interval with given end time. </p>
     *
     * @param   temporal    new end timepoint
     * @return  possibly changed copy of this interval
     * @throws  IllegalArgumentException if new end is before start
     * @since   1.3
     */
    /*[deutsch]
     * <p>Liefert eine Kopie dieses Intervalls mit der angegebenen Endzeit. </p>
     *
     * @param   temporal    new end timepoint
     * @return  possibly changed copy of this interval
     * @throws  IllegalArgumentException if new end is before start
     * @since   1.3
     */
    public abstract ChronoInterval<T> withEnd(T temporal);

    /**
     * <p>Determines if this interval has finite boundaries. </p>
     *
     * @return  {@code true} if start and end are finite else {@code false}
     * @since   1.3
     */
    /*[deutsch]
     * <p>Ermittelt, ob dieses Intervall endliche Grenzen hat. </p>
     *
     * @return  {@code true} if start and end are finite else {@code false}
     * @since   1.3
     */
    public boolean isFinite() {

        return !(this.start.isInfinite() || this.end.isInfinite());

    }

    /**
     * <p>Determines if this interval is empty. </p>
     *
     * @return  {@code true} if this interval does not contain any time point
     *          else {@code false}
     * @since   1.3
     */
    /*[deutsch]
     * <p>Ermittelt, ob dieses Intervall leer ist. </p>
     *
     * @return  {@code true} if this interval does not contain any time point
     *          else {@code false}
     * @since   1.3
     */
    public boolean isEmpty() {

        return (
            this.isFinite()
            && this.start.getTemporal().isSimultaneous(this.end.getTemporal())
            && (this.start.getEdge() != this.end.getEdge())); // half-open

    }

    /**
     * <p>Queries if given time point belongs to this interval. </p>
     *
     * @param   temporal    time point to be queried, maybe {@code null}
     * @return  {@code true} if given time point is not {@code null} and
     *          belongs to this interval else {@code false}
     * @since   1.3
     */
    /*[deutsch]
     * <p>Ermittelt, ob der angegebene Zeitpunkt zu diesem Intervall
     * geh&ouml;rt. </p>
     *
     * @param   temporal    time point to be queried, maybe {@code null}
     * @return  {@code true} if given time point is not {@code null} and
     *          belongs to this interval else {@code false}
     * @since   1.3
     */
    public boolean contains(T temporal) {

        if (temporal == null) {
            return false;
        }

        boolean startCondition;

        if (this.start.isInfinite()) {
            startCondition = true;
        } else if (this.start.isOpen()) {
            startCondition = this.start.getTemporal().isBefore(temporal);
        } else { // closed
            startCondition = !this.start.getTemporal().isAfter(temporal);
        }

        if (!startCondition) {
            return false; // short-cut
        }

        boolean endCondition;

        if (this.end.isInfinite()) {
            endCondition = true;
        } else if (this.end.isOpen()) {
            endCondition = this.end.getTemporal().isAfter(temporal);
        } else { // closed
            endCondition = !this.end.getTemporal().isBefore(temporal);
        }

        return endCondition;

    }

    @Override
    public final boolean equals(Object obj) {

        if (this == obj) {
            return true;
        } else if (obj instanceof ChronoInterval) {
            ChronoInterval<?> that = (ChronoInterval<?>) obj;
            return (
                this.start.equals(that.start)
                && this.end.equals(that.end)
                && this.getTimeLine().equals(that.getTimeLine())
            );
        } else {
            return false;
        }

    }

    @Override
    public final int hashCode() {

        return (17 * this.start.hashCode() + 37 * this.end.hashCode());

    }

    /**
     * <p>Yields a descriptive string using the standard output
     * of the method {@code toString()} of start and end. </p>
     *
     * @return  String
     */
    /*[deutsch]
     * <p>Liefert eine Beschreibung, die auf der Standardausgabe von
     * {@code toString()} angewandt auf Start und Ende beruht.. </p>
     *
     * @return  String
     */
    @Override
    public final String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append(this.start.isOpen() ? '(' : '[');
        sb.append(
            this.start.isInfinite()
            ? "-\u221E"
            : this.start.getTemporal());
        sb.append('/');
        sb.append(
            this.end.isInfinite()
            ? "+\u221E"
            : this.end.getTemporal());
        sb.append(this.end.isOpen() ? ')' : ']');
        return sb.toString();

    }

    /**
     * <p>Equivalent to
     * {@code toString(formatter, BracketPolicy.SHOW_WHEN_NON_STANDARD}. </p>
     *
     * @param   formatter   format object for printing start and end
     * @return  formatted string in format {start}/{end}
     * @since   1.3
     * @see     BracketPolicy#SHOW_WHEN_NON_STANDARD
     * @see     #toString(ChronoFormatter, BracketPolicy)
     */
    /*[deutsch]
     * <p>Entspricht
     * {@code toString(formatter, BracketPolicy.SHOW_WHEN_NON_STANDARD}. </p>
     *
     * @param   formatter   format object for printing start and end
     * @return  formatted string in format {start}/{end}
     * @since   1.3
     * @see     BracketPolicy#SHOW_WHEN_NON_STANDARD
     * @see     #toString(ChronoFormatter, BracketPolicy)
     */
    public String toString(ChronoFormatter<T> formatter) {

        return this.toString(formatter, BracketPolicy.SHOW_WHEN_NON_STANDARD);

    }

    /**
     * <p>Prints the start and end separated by a slash using given
     * formatter. </p>
     *
     * <p>Note: Infinite boundaries are printed either as &quot;-&#x221E;&quot;
     * or &quot;+&#x221E;&quot;. Example for an ISO-representation: </p>
     *
     * <pre>
     *  DateInterval interval =
     *      DateInterval.between(
     *          PlainDate.of(2014, 1, 31),
     *          PlainDate.of(2014, 4, 2));
     *  System.out.println(
     *      interval.toString(
     *          Iso8601Format.BASIC_CALENDAR_DATE,
     *          BracketPolicy.SHOW_NEVER));
     *  // output: 20140131/20140402
     * </pre>
     *
     * @param   formatter   format object for printing start and end
     * @param   policy      strategy for printing interval boundaries
     * @return  formatted string in format {start}/{end}
     * @since   1.3
     */
    /*[deutsch]
     * <p>Formatiert den Start und das Ende getrennt mit einem
     * Schr&auml;gstrich unter Benutzung des angegebenen Formatierers. </p>
     *
     * <p>Hinweis: Unendliche Intervallgrenzen werden entweder als
     * &quot;-&#x221E;&quot; oder &quot;+&#x221E;&quot; ausgegeben.
     * Beispiel f&uuml;r eine ISO-Darstellung: </p>
     *
     * <pre>
     *  DateInterval interval =
     *      DateInterval.between(
     *          PlainDate.of(2014, 1, 31),
     *          PlainDate.of(2014, 4, 2));
     *  System.out.println(
     *      interval.toString(
     *          Iso8601Format.BASIC_CALENDAR_DATE,
     *          BracketPolicy.SHOW_NEVER));
     *  // output: 20140131/20140402
     * </pre>
     *
     * @param   formatter   format object for printing start and end
     * @param   policy      strategy for printing interval boundaries
     * @return  formatted string in format {start}/{end}
     * @since   1.3
     */
    public String toString(
        ChronoFormatter<T> formatter,
        BracketPolicy policy
    ) {

        boolean showBoundaries = policy.display(this);
        StringBuilder sb = new StringBuilder(64);

        if (showBoundaries) {
            sb.append(this.start.isOpen() ? '(' : '[');
        }

        if (this.start.isInfinite()) {
            sb.append("-\u221E");
        } else {
            formatter.print(this.start.getTemporal(), sb);
        }

        sb.append('/');

        if (this.end.isInfinite()) {
            sb.append("+\u221E");
        } else {
            formatter.print(this.end.getTemporal(), sb);
        }

        if (showBoundaries) {
            sb.append(this.end.isOpen() ? ')' : ']');
        }

        return sb.toString();

    }

    /**
     * <p>Liefert die zugeh&ouml;rige Zeitachse. </p>
     *
     * @return  associated {@code TimeLine}
     * @since   1.3
     */
    protected abstract TimeLine<T> getTimeLine();

    /**
     * <p>Liefert die Rechenbasis zur Ermittlung einer Dauer. </p>
     *
     * @return  Intervall mit {@code duration = ende - start}
     * @throws  UnsupportedOperationException wenn unendlich
     * @since   1.3
     */
    ChronoInterval<T> getCalculationBase() {

        if (!this.isFinite()) {
            throw new UnsupportedOperationException(
                "An infinite interval has no finite duration.");
        }

        T t1 = this.start.getTemporal();
        T t2 = this.end.getTemporal();
        TimeLine<T> timeline = this.getTimeLine();

        if (this.start.isOpen()) {
            t1 = timeline.stepForward(t1);
        } else if (this.end.isClosed()) {
            t2 = timeline.stepForward(t2);
        } else {
            return this; // short-cut
        }

        return on(timeline).between(t1, t2); // half-open

    }

}
