package net.fortytwo.ripple.libs.stream.ranking;

import net.fortytwo.flow.Sink;
import net.fortytwo.ripple.ListMemoizer;
import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.model.Closure;
import net.fortytwo.ripple.model.ModelConnection;
import net.fortytwo.ripple.model.Operator;
import net.fortytwo.ripple.model.RippleList;
import net.fortytwo.ripple.model.RippleValue;
import net.fortytwo.ripple.model.RippleValueComparator;
import net.fortytwo.ripple.model.StackMapping;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Note: for synchronous evaluation only.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class RankingEvaluatorHelper {
    private static final Logger LOGGER = Logger.getLogger(RankingEvaluatorHelper.class);

    // A prioritized queue of active (still-to-be-reduced) stacks
    private final PriorityQueue<RankingContext> queue;

    // A ranked list of intermediate results
    private final List<RankingContext> resultList;

    // A set of intermediate results.
    private final ListMemoizer<RippleValue, RankingContext> resultMemos;

    public RankingEvaluatorHelper(final RippleList arg,
                                  final ModelConnection mc) {
        queue = new PriorityQueue<RankingContext>(1, comparator);

        resultList = new LinkedList<RankingContext>();
        resultMemos = new ListMemoizer<RippleValue, RankingContext>(
                new RippleValueComparator(mc));

        handleOutput(new RankingContext(arg, mc));
    }

    /**
     * Spends one computational step on improving the ranking.
     *
     * @return whether it is possible to take further steps
     * @throws RippleException if the ranking can't be computed
     */
    public boolean reduceOnce(final ModelConnection mc) throws RippleException {
        //System.out.println("\treduceOnce()");
        if (!queue.isEmpty()) {
            //System.out.println("\t\tqueue NOT empty");
            //inputSink.put(queue.poll());
            reduce(queue.poll().getStack(), mc);

            return !queue.isEmpty();
        } else {
            //System.out.println("\t\tqueue is empty");
            return false;
        }
    }

    /**
     * @return an ordered list of ranking results
     */
    public List<RankingContext> getResults() {
        Collections.sort(resultList, comparator);
        //for (RankingContext c : resultList) {
        //    System.out.println("\tresult: " + c);
        //}
        return resultList;
    }

    private void handleOutput(final RankingContext c) {
        //System.out.println("adding intermediate: " + c.getStack());
        if (c.getStack().isNil() || null == c.getStack().getFirst().getMapping()) {
            RankingContext other = resultMemos.get(c.getStack());

            if (null == other) {
                resultMemos.put(c.getStack(), c);
                resultList.add(c);
            } else {
                other.setPriority(other.getPriority() + c.getPriority());
            }
        } else {
            queue.add(c);
        }
    }

    private final Comparator<RankingContext> comparator = new Comparator<RankingContext>() {
        public int compare(final RankingContext first,
                           final RankingContext second) {
            // This orders items from highest to lowest priority.
            return ((Double) second.getPriority()).compareTo(first.getPriority());
        }
    };

    private final Sink<RippleList> outputSink = new Sink<RippleList>() {
        public void put(final RippleList c) throws RippleException {
            //System.out.println("got this output: " + c.getStack());
            // TODO
            /*
            if (c instanceof RankingContext) {
                handleOutput((RankingContext) c);
            } */
        }
    };

    ////////////////////////////////////////////////////////////////////////////

    private void reduce(final RippleList arg,
                        final ModelConnection mc) throws RippleException {
        RippleList stack = arg;
        RippleList ops = mc.list();

        while (true) {
            RippleValue first = stack.getFirst();

            if (stack.isNil() || null == first.getMapping()) {
                if (ops.isNil()) {
                    outputSink.put(stack);
                    return;
                } else {
                    Closure c = new Closure(ops.getFirst().getMapping(), first);
                    stack = stack.getRest().push(new Operator(c));
                    ops = ops.getRest();
                }
            } else {
                StackMapping f = first.getMapping();

                if (0 == f.arity()) {
                    try {
                        if (ops.isNil()) {
                            f.apply(stack.getRest(), outputSink, mc);
                        } else {
                            f.apply(stack.getRest(), new SpecialSink(ops, mc), mc);
                        }
                    } catch (Throwable t) {
                        // To keep things simple, just eat any errors.
                        LOGGER.error("error in expression reduction", t);
                    }
                    return;
                } else {
                    ops = ops.push(first);
                    stack = stack.getRest();
                }
            }
        }
    }

    private class SpecialSink implements Sink<RippleList> {
        private final RippleList ops;
        private final ModelConnection mc;

        public SpecialSink(final RippleList ops,
                           final ModelConnection mc) {
            this.ops = ops;
            this.mc = mc;
        }

        public void put(final RippleList arg) throws RippleException {
            RippleList stack = arg;

            RippleList cur = ops;
            while (!cur.isNil()) {
                stack = stack.push(cur.getFirst());
                cur = cur.getRest();
            }

            reduce(stack, mc);
        }
    }
}
