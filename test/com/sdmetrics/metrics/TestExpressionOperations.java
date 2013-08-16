package com.sdmetrics.metrics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import com.sdmetrics.math.HashMultiSet;
import com.sdmetrics.model.ModelElement;
import com.sdmetrics.test.MetricTestContext;

public class TestExpressionOperations {

	MetricsEngine me;

	MetricTestContext mtc;

	Variables vars;

	@Before
	public void loadTestFixture() throws Exception {
		mtc = MetricTestContext.getStandardContext04();
		me = mtc.me;

		Collection<String> simpleSet = new HashSet<String>();
		simpleSet.add("Sleepy");
		simpleSet.add("Grumpy");
		simpleSet.add("Sneezy");
		simpleSet.add("Happy");

		Collection<String> multiSet = new HashMultiSet<String>(4);
		multiSet.add("Doc");
		multiSet.add("Grumpy");
		multiSet.add("Doc");

		vars = new Variables(mtc.getElement("Person", "class"));
		vars.setVariable("simpleSet", simpleSet);
		vars.setVariable("multiSet", multiSet);
	}

	@Test
	public void numericalOperators() throws SDMetricsException {
		assertEquals(new Float(-3),
				me.evalExpression(null, mtc.parse("-(3)"), null));
		assertEquals(new Float(5.15),
				me.evalExpression(null, mtc.parse("+(5.15)"), null));
		Float euler = (Float) me
				.evalExpression(null, mtc.parse("exp(1)"), null);
		assertEquals(new Float(Math.E), euler);
		assertEquals(1.0D, ((Float) me.evalExpression(null,
				mtc.parse("ln(" + euler + ")"), null)).doubleValue(),
				0.0000001D);
		assertEquals(new Float(2),
				me.evalExpression(null, mtc.parse("sqrt(4)"), null));
		assertEquals(new Float(3),
				me.evalExpression(null, mtc.parse("abs(-3)"), null));
		assertEquals(new Float(4),
				me.evalExpression(null, mtc.parse("floor(4.999999)"), null));
		assertEquals(new Float(5),
				me.evalExpression(null, mtc.parse("ceil(4.000001)"), null));
		assertEquals(new Float(6),
				me.evalExpression(null, mtc.parse("round(5.5)"), null));
		assertEquals(new Float(7),
				me.evalExpression(null, mtc.parse("round(7.499999)"), null));
		try {
			me.evalExpression(null, mtc.parse("!0"), null);
			fail("Exception expected");
		} catch (SDMetricsException ex) {
			assertEquals("Unknown operation '!'.", ex.getMessage());
		}

		assertEquals(new Float(8),
				me.evalExpression(null, mtc.parse("3+5"), null));
		assertEquals(new Float(9),
				me.evalExpression(null, mtc.parse("12-3"), null));
		assertEquals(new Float(10),
				me.evalExpression(null, mtc.parse("0.5*20"), null));
		assertEquals(new Float(11),
				me.evalExpression(null, mtc.parse("33/3"), null));
		assertEquals(new Float(16),
				me.evalExpression(null, mtc.parse("256^0.5"), null));

		try {
			me.evalExpression(null, mtc.parse("1&1"), null);
			fail("Exception expected");
		} catch (SDMetricsException ex) {
			assertEquals("Unknown operation '&'.", ex.getMessage());
		}

		try {
			me.evalExpression(null, mtc.parse("3*'b'"), null);
			fail("Exception expected");
		} catch (SDMetricsException ex) {
			assertEquals("Operator '*' requires numerical operand.",
					ex.getMessage());
		}
	}

	@Test
	public void parseNumber() throws SDMetricsException {
		assertEquals(Integer.valueOf(3),
				me.evalExpression(null, mtc.parse("parsenumber('3')"), null));
		assertEquals(Integer.valueOf(42), me.evalExpression(null,
				mtc.parse("parsenumber('+42.0')"), null));
		assertEquals(new Float(-0.0515), me.evalExpression(null,
				mtc.parse("parsenumber('-5.15e-2')"), null));

		try {
			me.evalExpression(null, mtc.parse("parsenumber('one')"), null);
			fail("Exception expected");
		} catch (SDMetricsException ex) {
			assertEquals("Could not parse as number: 'one'", ex.getMessage());
		}
	}

	@Test
	public void numericalComparisons() throws SDMetricsException {
		evalBoolean(true, "1>0");
		evalBoolean(false, "1.2>3.4");
		evalBoolean(true, "-1<1");
		evalBoolean(false, "1<0");
		evalBoolean(true, "1>=1.0");
		evalBoolean(false, "0>=2");
		evalBoolean(true, "1.0<=1.0");
		evalBoolean(false, "1.0<=0.9");
		evalBoolean(true, "1!=2");
		evalBoolean(false, "1.0!=1.0");
		evalBoolean(true, "1.0=1.0");
		evalBoolean(false, "0=1");
		try {
			me.evalBooleanExpression(null, mtc.parse("0+1"), null);
			fail("Exception expected");
		} catch (SDMetricsException ex) {
			assertEquals("Unknown boolean operation '+'.", ex.getMessage());
		}
	}

	@Test
	public void stringOperators() throws SDMetricsException {
		assertEquals(Integer.valueOf(9),
				me.evalExpression(null, mtc.parse("length('SDMetrics')"), null));
		assertEquals("sdmetrics", me.evalExpression(null,
				mtc.parse("tolowercase('SDMetrics')"), null));

		assertEquals("LaurelHardy",
				me.evalExpression(null, mtc.parse("'Laurel'+'Hardy'"), null));
		assertEquals("Fahrenheit 451",
				me.evalExpression(null, mtc.parse("'Fahrenheit '+451"), null));
	}

	@Test
	public void qualifiedNameFunction() throws SDMetricsException {

		ModelElement attribute = mtc.getElement(0, "attribute");
		assertEquals("testModel04.Person.firstName", me.evalExpression(
				attribute, mtc.parse("qualifiedname(self)"), null));
		assertEquals("", me.evalExpression(attribute,
				mtc.parse("qualifiedname('')"), null));
		try {
			me.evalExpression(attribute, mtc.parse("qualifiedname('Haensel')"),
					null);
			fail("Exception expected");
		} catch (SDMetricsException ex) {
			assertEquals(
					"Operator 'qualifiedname' can only be applied to model elements.",
					ex.getMessage());
		}
		try {
			me.evalExpression(attribute, mtc.parse("qualifiedname(0)"), null);
			fail("Exception expected");
		} catch (SDMetricsException ex) {
			assertEquals(
					"Operator 'qualifiedname' can only be applied to model elements.",
					ex.getMessage());
		}
	}

	@Test
	public void typeOfFunction() throws SDMetricsException {

		ModelElement attribute = mtc.getElement(0, "attribute");
		assertEquals("",
				me.evalExpression(attribute, mtc.parse("typeof('')"), null));
		assertEquals("attribute",
				me.evalExpression(attribute, mtc.parse("typeof(_self)"), null));

		try {
			me.evalExpression(attribute, mtc.parse("typeof('wheat')"), null);
			fail("Exception expected");
		} catch (SDMetricsException ex) {
			assertEquals(
					"Operator 'typeof' can only be applied to model elements.",
					ex.getMessage());
		}
		try {
			me.evalExpression(attribute, mtc.parse("typeof(0)"), null);
			fail("Exception expected");
		} catch (SDMetricsException ex) {
			assertEquals(
					"Operator 'typeof' can only be applied to model elements.",
					ex.getMessage());
		}
	}

	@Test
	public void stringComparisons() throws SDMetricsException {
		evalBoolean(true, "'horse'='horse'");
		evalBoolean(false, "'sauce for the goose'='sauce for the gander'");
		evalBoolean(true, "'error'!='disaster'");
		evalBoolean(false, "'bargain'!='bargain'");
		evalBoolean(true, "'also'<'sprach'");
		evalBoolean(false, "'also'<'Sprach'");
		evalBoolean(true, "'sprach'>'Zarathustra'");
		evalBoolean(false, "'Sprach'>'Zarathustra'");
		evalBoolean(true, "'the same'<='the same'");
		evalBoolean(false, "3<='2'");
		evalBoolean(true, "'the same'>='the same'");
		evalBoolean(false, "'2'>=3");

		evalBoolean(true, "'childhood' startswith 'child'");
		evalBoolean(false, "'childhood' startswith 25");
		evalBoolean(true, "'marriage' endswith 'age'");
		evalBoolean(false, "'marriage' endswith 'divorce'");
	}

	@Test
	public void stringFunctions() throws SDMetricsException {
		evalBoolean(false, "startswithcapital('getTan')");
		evalBoolean(true, "startswithcapital('')");
		evalBoolean(true, "startswithcapital('MAXLEN')");

		evalBoolean(true, "startswithlowercase('getTan')");
		evalBoolean(true, "startswithlowercase('')");
		evalBoolean(false, "startswithlowercase('MAXLEN')");

		evalBoolean(true, "islowercase('sdmetrics')");
		evalBoolean(true, "islowercase('')");
		evalBoolean(false, "islowercase('SDMetrics')");

		evalBoolean(true, "'public' onlist Java");
		evalBoolean(false, "'nil' onlist 'Java'");
		evalBoolean(true, "'void' onlist ('Ja'+'va')");
	}

	@Test
	public void booleanOperators() throws SDMetricsException {
		evalBoolean(true, "1=1 | 1=0");
		evalBoolean(true, "1=0 | 1=1");
		evalBoolean(false, "1=0 | 2=0");
		evalBoolean(true, "!(1=0)");
		evalBoolean(false, "!(1=1)");
		evalBoolean(false, "1=0 & 1=1");
		evalBoolean(false, "1=1 & 1=0");
		evalBoolean(true, "1=1 & 2=2");
	}

	@Test
	public void testInOperatorBool() throws SDMetricsException {

		ModelElement pers = mtc.getElement("Person", "class");
		assertFalse(me.evalBooleanExpression(pers,
				mtc.parse("'Bashful'->simpleSet"), vars));
		assertTrue(me.evalBooleanExpression(pers,
				mtc.parse("'Grumpy'->simpleSet"), vars));
		assertFalse(me.evalBooleanExpression(pers,
				mtc.parse("'Bashful'->multiSet"), vars));
		assertTrue(me.evalBooleanExpression(pers, mtc.parse("'Doc'->multiSet"),
				vars));
	}

	@Test
	public void testInstanceOfOperator() throws SDMetricsException {

		ModelElement pers = mtc.getElement("Person", "class");
		ModelElement model = mtc.getElement(0, "model");
		vars.setVariable("_model", model);
		assertTrue(me.evalBooleanExpression(pers,
				mtc.parse("instanceof('class')"), vars));
		assertFalse(me.evalBooleanExpression(pers,
				mtc.parse("instanceof(_model)"), vars));
		assertTrue(me.evalBooleanExpression(pers,
				mtc.parse("instanceof(_model,'package')"), vars));
		assertFalse(me.evalBooleanExpression(pers,
				mtc.parse("instanceof(_model.context,'package')"), vars));

		try {
			assertTrue(me.evalBooleanExpression(pers,
					mtc.parse("instanceof('interface','class')"), null));
			fail("Exception expected");
		} catch (SDMetricsException ex) {
			assertEquals(
					"First argument of binary function instanceof must yield a model element",
					ex.getMessage());
		}

		try {
			assertTrue(me.evalBooleanExpression(pers,
					mtc.parse("instanceof('politician')"), null));
			fail("Exception expected");
		} catch (SDMetricsException ex) {
			assertEquals("Unknown base type 'politician'", ex.getMessage());
		}

	}

	private void evalBoolean(boolean expected, String expression)
			throws SDMetricsException {
		assertTrue(expected == me.evalBooleanExpression(null,
				mtc.parse(expression), null));
	}

	@Test
	public void specialOperators() throws SDMetricsException {
		ModelElement pers = mtc.getElement("Person", "class");
		assertEquals(Integer.valueOf(2),
				me.evalExpression(pers, mtc.parse("self.NumAttr"), null));
		assertEquals("",
				me.evalExpression(pers, mtc.parse("context.NumAttr"), null));
		assertEquals("",
				me.evalExpression(pers, mtc.parse("_self.NumStates"), null));
		assertEquals("",
				me.evalExpression(pers, mtc.parse("'Donald'.'Duck'"), null));

		ModelElement class5 = mtc.getElement("Class5", "class");
		assertSame(
				mtc.getElement("package1_1", "package"),
				me.evalExpression(class5,
						mtc.parse("context upto name='package1_1'"), null));
		assertSame(
				"",
				me.evalExpression(class5,
						mtc.parse("context upto name='Walter'"), null));
		assertSame(mtc.getElement("package1", "package"), me.evalExpression(
				class5, mtc.parse("context topmost name startswith 'package'"),
				null));
	}

	@Test
	public void setFunctions() throws SDMetricsException {
		ModelElement pers = mtc.getElement("Person", "class");
		assertEquals(Integer.valueOf(4),
				me.evalExpression(pers, mtc.parse("size(simpleSet)"), vars));
		assertEquals(Integer.valueOf(4),
				me.evalExpression(pers, mtc.parse("flatsize(simpleSet)"), vars));
		assertEquals(Integer.valueOf(3),
				me.evalExpression(pers, mtc.parse("size(multiSet)"), vars));
		assertEquals(Integer.valueOf(2),
				me.evalExpression(pers, mtc.parse("flatsize(multiSet)"), vars));

		assertTrue(me.evalBooleanExpression(pers,
				mtc.parse("isunique(simpleSet)"), vars));
		assertFalse(me.evalBooleanExpression(pers,
				mtc.parse("isunique(multiSet)"), vars));
		Collection<?> multiSet = me.evalSetExpression(pers,
				mtc.parse("multiSet"), vars);
		multiSet.remove("Doc");
		assertTrue(me.evalBooleanExpression(pers,
				mtc.parse("isunique(multiSet)"), vars));
	}

	@Test
	public void setOperations() throws SDMetricsException {
		ModelElement pers = mtc.getElement("Person", "class");

		Collection<?> attributes = me.evalSetExpression(pers,
				mtc.parse("_self.AttrSet"), null);
		assertEquals(2, attributes.size());
		attributes = me.evalSetExpression(pers, mtc.parse("'Noodles'.AttrSet"),
				null);
		assertEquals(0, attributes.size());
		attributes = me.evalSetExpression(pers, mtc.parse("self.NoSuchSet"),
				null);
		assertEquals(0, attributes.size());

		assertEquals(Integer.valueOf(0), me.evalExpression(pers,
				mtc.parse("'Bashful'->simpleSet"), vars));
		assertEquals(Integer.valueOf(1),
				me.evalExpression(pers, mtc.parse("'Grumpy'->simpleSet"), vars));
		assertEquals(Integer.valueOf(2),
				me.evalExpression(pers, mtc.parse("'Doc'->multiSet"), vars));

		Collection<?> union = me.evalSetExpression(pers,
				mtc.parse("simpleSet+multiSet"), vars);
		assertEquals(HashMultiSet.class, union.getClass());
		assertEquals(7, union.size());
		union = me.evalSetExpression(pers, mtc.parse("multiSet+simpleSet"),
				vars);
		assertEquals(7, union.size());
		union = me.evalSetExpression(pers, mtc.parse("simpleSet+simpleSet"),
				vars);
		assertEquals(HashSet.class, union.getClass());
		assertEquals(4, union.size());
		union = me
				.evalSetExpression(pers, mtc.parse("multiSet+multiSet"), vars);
		assertEquals(HashMultiSet.class, union.getClass());
		assertEquals(6, union.size());

		Collection<?> difference = me.evalSetExpression(pers,
				mtc.parse("multiSet-simpleSet"), vars);
		assertEquals(HashMultiSet.class, union.getClass());
		assertEquals(2, difference.size());
		difference = me.evalSetExpression(pers,
				mtc.parse("simpleSet-simpleSet"), vars);
		assertEquals(HashSet.class, difference.getClass());
		assertEquals(0, difference.size());

		Collection<?> intersection = me.evalSetExpression(pers,
				mtc.parse("simpleSet*multiSet"), vars);
		assertEquals(HashSet.class, intersection.getClass());
		assertEquals(1, intersection.size());
		intersection = me.evalSetExpression(pers,
				mtc.parse("multiSet*multiSet"), vars);
		assertEquals(HashMultiSet.class, intersection.getClass());
		assertEquals(3, intersection.size());
		intersection = me.evalSetExpression(pers,
				mtc.parse("simpleSet*simpleSet"), vars);
		assertEquals(HashSet.class, intersection.getClass());
		assertEquals(4, intersection.size());

	}

}
