package org.maple.core;

/* to execute the test codes, copy junit.jar and hamcrest.jar
 * to IntelliJ repository/lib repo
 */

import org.junit.Test;

import static org.junit.Assert.*;
import java.util.LinkedList;
import java.util.HashSet;


public class TraceTreeTest {

    int[] data = {0,0,0,0,0,2,0,0,0,0,0,4,8,0,69,0,0,84,73,114,0,0,64,1,
            29,50,10,0,0,4,10,0,0,2,0,0,241,88,120,214,0,1,250,39,16,
            85,0,0,0,0,192,127,12,0,0,0,0,0,16,17,18,19,20,21,22,23,
            24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,
            43,44,45,46,47,48,49,50,51,52,53,54,55};
    int PORT = 10;

    @Test
    // empty tree augmented by empty trace with outcome
    public void testAugment1() {

        TraceTree tree = new TraceTree();

        assertNull(tree.root);

        LinkedList<TraceItem> emptyTrace = new LinkedList<TraceItem>();  // import of augment
        int[] outcome = {1,2,3};   // user function return
        tree.augment(emptyTrace, Route.toPorts(outcome));

        // UnitTest will stop if any assert.. statement fails
        assertNotNull(tree.root);
        assertTrue(tree.root instanceof L); // L is leaf
        assertArrayEquals(outcome, ((L) tree.root).outcome);  // cast tree.root to L leaf

        byte[] frameBytes = makeFrameBytes(data);
        Ethernet frame = new Ethernet();  // create import
        frame.deserialize(frameBytes, 0, frameBytes.length);
        assertNotNull(tree.evaluate(PORT, frame));  // tree.evaluate: find all the L fulfill the requirements PORT and frame
        assertArrayEquals(outcome, tree.evaluate(PORT, frame));
    }


    @Test
    // empty tree augmented by non-empty trace(one inPort) with outcome
    public void testAugment2() {

        TraceTree tree = new TraceTree();

        assertNull(tree.root);


        LinkedList<TraceItem> trace = new LinkedList<TraceItem>();
        trace.add(TraceItemV.inPort(PORT));  // trace's field inPort is PORT
        int[] outcome = {1,2,3};
        tree.augment(trace, Route.toPorts(outcome));

        assertNotNull(tree.root);
        assertTrue(tree.root instanceof V); // whether tree.root is V

        V node = (V) tree.root;

        assertNull(node.getChild(PORT+1));  // should return the child fulfill requirement
        assertNotNull(node.getChild(PORT));
        assertTrue(node.getChild(PORT) instanceof L);
        L leaf = (L) node.getChild(PORT);
        assertArrayEquals(outcome, leaf.outcome);

        byte[] frameBytes = makeFrameBytes(data);
        Ethernet frame = new Ethernet();
        frame.deserialize(frameBytes, 0, frameBytes.length);
        assertNull(tree.evaluate(PORT + 1, frame));
        assertNotNull(tree.evaluate(PORT, frame));
        assertArrayEquals(outcome, tree.evaluate(PORT, frame));

    }










    @Test
    // empty tree augmented by non-empty trace(one inPort and one ethDst) with outcome
    // non-empty tree augmented by non-empty trace
    public void testAugment3() {
        TraceTree tree = new TraceTree();

        assertNull(tree.root);

        LinkedList<TraceItem> trace = new LinkedList<TraceItem>();

        trace.add(TraceItemV.inPort(1));
        trace.add(TraceItemV.ethDst(0x02));
        int[] outcome={2};

        tree.augment(trace, Route.toPorts(outcome));

        assertNotNull(tree.root);
        V node = (V) tree.root;
        V node2 = (V) node.getChild(1);
        assertNotNull(node2);
        assertTrue(node2 instanceof V);
        // assertEquals(node2.field, trace.get(1).field);
        assertNull(node2.getChild(0x03));
        assertTrue(node2.getChild(0x02) instanceof L);
        L leaf = (L) node2.getChild(0x02);
        assertArrayEquals(leaf.outcome, outcome);

        LinkedList<TraceItem> trace2 = new LinkedList<TraceItem>();

        trace2.add(TraceItemV.inPort(1));
        trace2.add(TraceItemV.ethDst(0x05));
        int[] outcome2 = {5};

        // non-empty tree
        tree.augment(trace2, Route.toPorts(outcome2));
        node = (V) tree.root;
        node2 = (V) node.getChild(1);

        assertNotNull(tree.root);

        // make sure tree.root.getChild(1) keeps the same
        assertNotNull(node2);
        assertTrue(node2 instanceof V);
        // assertEquals(node2.field, trace.get(1).field);
        assertNull(node2.getChild(0x03));
        assertTrue(node2.getChild(0x02) instanceof L);

        // test tree.root.getChild(1).getChild(0x05)
        assertNotNull(node2.getChild(0x05));
        assertTrue(node2.getChild(0x05) instanceof L);
        L leaf2 = (L) node2.getChild(0x05);
        assertNotEquals(leaf2.outcome, outcome);
        assertArrayEquals(leaf2.outcome, outcome2);
    }


    @Test
    // empty tree augmented by non-empty trace(one inPort and one ethDst) with outcome
    // non-empty tree augmented by empty trace
    public void testAugment4() {
        TraceTree tree = new TraceTree();

        assertNull(tree.root);

        LinkedList<TraceItem> trace = new LinkedList<TraceItem>();

        trace.add(TraceItemV.inPort(1));
        trace.add(TraceItemV.ethDst(0x02));
        int[] outcome={2};

        tree.augment(trace, Route.toPorts(outcome));

        assertNotNull(tree.root);
        V node = (V) tree.root;
        V node2 = (V) node.getChild(1);
        assertNotNull(node2);
        assertTrue(node2 instanceof V);
        // assertEquals(node2.field, trace.get(1).field);
        assertNull(node2.getChild(0x03));
        assertTrue(node2.getChild(0x02) instanceof L);
        L leaf = (L) node2.getChild(0x02);
        assertArrayEquals(leaf.outcome, outcome);

        LinkedList<TraceItem> trace2 = new LinkedList<TraceItem>();

        int[] outcome2 = {5};

        // non-empty tree
        tree.augment(trace2, Route.toPorts(outcome2));

        assertNotNull(tree.root);

        // make sure tree.root.getChild(1) keeps the same
        assertNotNull(node2);
        assertTrue(node2 instanceof V);
        // assertEquals(node2.field, trace.get(1).field);
        assertNull(node2.getChild(0x03));
        assertNotNull(node2.getChild(0x02));
        assertTrue(node2.getChild(0x02) instanceof L);
    }


    @Test
    // empty tree augmented by non-empty trace(one inPort and one ethDst) with outcome
    // then augmented by non-empty trace(one different inPort and one ethDst) with outcome
    public void testAugment5() {
        TraceTree tree = new TraceTree();

        assertNull(tree.root);

        LinkedList<TraceItem> trace = new LinkedList<TraceItem>();

        trace.add(TraceItemV.inPort(1));
        trace.add(TraceItemV.ethDst(0x02));
        int[] outcome={2};

        tree.augment(trace, Route.toPorts(outcome));

        assertNotNull(tree.root);
        V node = (V) tree.root;
        V node2 = (V) node.getChild(1);
        assertNotNull(node2);
        assertTrue(node2 instanceof V);
        // assertEquals(node2.field, trace.get(1).field);
        assertNull(node2.getChild(0x03));
        assertTrue(node2.getChild(0x02) instanceof L);
        L leaf = (L) node2.getChild(0x02);
        assertArrayEquals(leaf.outcome, outcome);

        LinkedList<TraceItem> trace2 = new LinkedList<TraceItem>();

        trace2.add(TraceItemV.inPort(3));
        trace2.add(TraceItemV.ethDst(0x05));
        int[] outcome2 = {5};

        // non-empty tree
        tree.augment(trace2, Route.toPorts(outcome2));

        assertNotNull(tree.root);
        V node3 = (V) node.getChild(3);

        assertNotNull(node3);
        assertTrue(node3 instanceof V);
        // assertEquals(node3.field, trace2.get(3).field);
        assertNull(node3.getChild(0x03));
        assertNull(node3.getChild(0x02));
        assertTrue(node3.getChild(0x05) instanceof L);
        L leaf2 = (L) node3.getChild(0x05);
        assertNotEquals(leaf2.outcome, outcome);
        assertArrayEquals(leaf2.outcome, outcome2);
    }










    @Test
    public void testItemEq() {
        assertEquals(TraceItemV.ethSrc(1),  TraceItemV.ethSrc(1));
        assertNotEquals(TraceItemV.ethSrc(1),  TraceItemV.ethSrc(2));
    }

    @Test
    // Compile: each leaf has a rule (priority, match, actions)
    public void testCompile1() {

        TraceTree tree;
        LinkedList<Rule> rulesExpected;
        Action action;
        LinkedList<TraceItem> trace;

        tree = new TraceTree();
        rulesExpected = new LinkedList<Rule>();
        rulesExpected.add(new Rule(0, Match.matchAny(), Action.Punt()));
        assertNotNull(tree.compile());
        assertEquals(rulesExpected, tree.compile());

        rulesExpected = new LinkedList<Rule>();
        action = Action.ToPorts(1,2,3);
        // actions.add(Action.ToPort(2));
        // actions.add(Action.ToPort(3));
        rulesExpected.add(new Rule(0, Match.matchAny(), action));
        LinkedList<TraceItem> emptyTrace = new LinkedList<TraceItem>();
        int[] outcome = {1,2,3};
        tree.augment(emptyTrace, Route.toPorts(outcome));
        assertEquals(rulesExpected, tree.compile());

        tree = new TraceTree();
        trace = new LinkedList<TraceItem>();
        trace.add(TraceItemV.inPort(PORT));
        tree.augment(trace, Route.toPorts(outcome));
        rulesExpected = new LinkedList<Rule>();
        rulesExpected.add(new Rule(0,
                Match.matchAny().add(TraceItemV.inPort(PORT)),
                action));
        assertEquals(rulesExpected, tree.compile());
    }


    @Test
    // Compile: test tree with both inPort and outcome
    public void testCompile2() {

        TraceTree tree;
        LinkedList<Rule> rulesExpected;
        Action action;
        LinkedList<TraceItem> trace;

        // null tree.compile() equals to rulesExpected blank
        tree = new TraceTree();
        rulesExpected = new LinkedList<Rule>();
        rulesExpected.add(new Rule(0, Match.matchAny(), Action.Punt()));
        assertNotNull(tree.compile());
        assertEquals(rulesExpected, tree.compile());

        trace = new LinkedList<TraceItem>();
        trace.add(TraceItemV.inPort(PORT));
        int[] outcome = {1, 2, 3};
        tree.augment(trace, Route.toPorts(outcome));

        rulesExpected = new LinkedList<Rule>();
        action = Action.ToPorts(1,2,3);
        rulesExpected.add(new Rule(0, Match.matchAny().add(TraceItemV.inPort(PORT)), action));

        assertEquals(rulesExpected, tree.compile());

    }




    // Java does not support byte literals; therefore, we need to convert
    // an array of ints to array of bytes.
    byte[] makeFrameBytes(int[] data) {
        byte[] frame = new byte[data.length];
        for (int i = 0; i < frame.length; i++) {
            frame[i] = (byte) data[i];
        }
        return frame;
    }
}
