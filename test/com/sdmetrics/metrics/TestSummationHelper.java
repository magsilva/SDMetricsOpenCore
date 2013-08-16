package com.sdmetrics.metrics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import com.sdmetrics.model.ModelElement;
import com.sdmetrics.test.MetricTestContext;

public class TestSummationHelper {

	MetricTestContext mtc;
	ProcedureAttributes procAttrs;
	ModelElement pck1;
	ModelElement pck1_1;

	@Before
	public void loadTestFixture() throws Exception {
		mtc = MetricTestContext.getStandardContext04();
		procAttrs = new ProcedureAttributes();
		pck1 = mtc.getElement("package1", "package");
		pck1_1 = mtc.getElement("package1_1", "package");
	}

	@Test
	public void simpleCount() throws SDMetricsException {
		SummationHelper sh = new SummationHelper(mtc.me, procAttrs);

		assertEquals(Integer.valueOf(0), sh.getTotal());
		sh.add(pck1, null);
		assertEquals(Integer.valueOf(1), sh.getTotal());
		sh.add(pck1_1, null);
		assertEquals(Integer.valueOf(2), sh.getTotal());
	}

	@Test
	public void sumExpression() throws SDMetricsException {
		procAttrs.setExpressionNode("sum", mtc.parse("NameLength"));
		procAttrs.setExpressionNode("stat", mtc.parse("sum"));
		SummationHelper sh = new SummationHelper(mtc.me, procAttrs);
		sh.add(pck1, null);
		assertEquals(Integer.valueOf(8), sh.getTotal());
		sh.add(pck1_1, null);
		assertEquals(Integer.valueOf(18), sh.getTotal());
	}

	@Test
	public void maximum() throws SDMetricsException {
		procAttrs.setExpressionNode("sum", mtc.parse("NameLength"));
		procAttrs.setExpressionNode("stat", mtc.parse("max"));
		SummationHelper sh = new SummationHelper(mtc.me, procAttrs);
		assertEquals(Integer.valueOf(0), sh.getTotal());
		sh.add(pck1, null);
		assertEquals(Integer.valueOf(8), sh.getTotal());
		sh.add(pck1_1, null);
		assertEquals(Integer.valueOf(10), sh.getTotal());
		sh.add(pck1, null);
		assertEquals(Integer.valueOf(10), sh.getTotal());
	}

	@Test
	public void minimum() throws SDMetricsException {
		procAttrs.setExpressionNode("sum", mtc.parse("NameLength"));
		procAttrs.setExpressionNode("stat", mtc.parse("min"));
		SummationHelper sh = new SummationHelper(mtc.me, procAttrs);
		assertEquals(Integer.valueOf(0), sh.getTotal());
		sh.add(pck1_1, null);
		assertEquals(Integer.valueOf(10), sh.getTotal());
		sh.add(pck1, null);
		assertEquals(Integer.valueOf(8), sh.getTotal());
		sh.add(pck1_1, null);
		assertEquals(Integer.valueOf(8), sh.getTotal());
	}

	@SuppressWarnings("unused")
	@Test
	public void illegalStat() {
		procAttrs.setExpressionNode("stat", mtc.parse("mode"));
		try {
			new SummationHelper(mtc.me, procAttrs);
			fail("Exception expected");
		} catch (SDMetricsException ex) {
			assertEquals("Illegal value 'mode' for attribute 'stat'.",
					ex.getMessage());
		}
	}

	@Test
	public void directManipulation() throws SDMetricsException {
		SummationHelper sh = new SummationHelper(mtc.me, procAttrs);
		sh.add(3);
		assertEquals(Integer.valueOf(3), sh.getTotal());
		sh.raiseTo(7);
		assertEquals(Integer.valueOf(7), sh.getTotal());
		sh.add(5);
		assertEquals(Integer.valueOf(12), sh.getTotal());
		sh.raiseTo(9);
		assertEquals(Integer.valueOf(12), sh.getTotal());
		sh.raiseTo(12.75f);
		assertEquals(new Float(12.75), sh.getTotal());
	}
}
