package insomnia.implem.fsa.fta;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import insomnia.data.IEdge;
import insomnia.data.ITree;
import insomnia.data.TreeOp;
import insomnia.data.creational.ITreeBuilder;
import insomnia.data.regex.ITreeMatcher;
import insomnia.fsa.fta.IBUFTA;
import insomnia.implem.data.Trees;
import insomnia.implem.data.creational.TreeBuilder;
import insomnia.implem.fsa.fta.creational.BUBuilder;
import insomnia.implem.kv.data.KVLabel;
import insomnia.implem.kv.data.KVLabels;
import insomnia.implem.kv.data.KVValue;
import insomnia.implem.kv.data.KVValues;

public class TestAutomaton
{
	// =========================================================================

	@Test
	void match4()
	{
		ITreeBuilder<Object, String> tree  = new TreeBuilder<>();
		List<IEdge<Object, String>>  added = new ArrayList<>();

		tree.setRooted(false) //
			.child("a") //
			.child("b", added).end() //
			.child("c", added).endTerminal(); //

		IBUFTA<Object, String> bufta = new BUBuilder<>(tree).create();

		tree.reset().setRooted(false) //
			.child("a") //
			.child("b", added).end() //
			.child("c", added).endTerminal(14);

		assertEquals(true, bufta.matcher(tree).matches());

		tree.reset().setRooted(false) //
			.child("a") //
			.child("b", added).end() //
			.child("c", added).end(); //

		assertEquals(false, bufta.matcher(tree).matches());
	}

	@Test
	void matchRoot2()
	{
		ITreeBuilder<Object, String> tree  = new TreeBuilder<>();
		List<IEdge<Object, String>>  added = new ArrayList<>();

		tree.setRooted(false) //
			.child("a") //
			.child("b", added).end() //
			.child("c", added).end(); //

		IBUFTA<Object, String> bufta = new BUBuilder<>(tree).create();
		tree.setRooted(true);
		assertEquals(true, bufta.matcher(tree).matches());
		tree.setRooted(false);
		assertEquals(true, bufta.matcher(tree).matches());
	}

	@Test
	void matchRoot()
	{
		ITreeBuilder<Object, String> tree  = new TreeBuilder<>();
		List<IEdge<Object, String>>  added = new ArrayList<>();

		tree.setRooted(true) //
			.child("a") //
			.child("b", added).end() //
			.child("c", added).end(); //

		IBUFTA<Object, String> bufta = new BUBuilder<>(tree).create();
		assertEquals(true, bufta.matcher(tree).matches());
		tree.setRooted(false);
		assertEquals(false, bufta.matcher(tree).matches());
	}

	@Test
	void match2()
	{
		ITreeBuilder<Object, String> tree  = new TreeBuilder<>();
		List<IEdge<Object, String>>  added = new ArrayList<>();

		tree.setRooted(false) //
			.child("a") //
			.child("b", added).end() //
			.child("c", added).end(); //

		IBUFTA<Object, String> bufta = new BUBuilder<>(tree).create();

		for (IEdge<Object, String> edge : added)
			tree.setCurrentNode(edge.getChild()).child("xx");

		tree.parent("pp");
		assertEquals(true, bufta.matcher(tree).matches());
	}

	@Test
	void match()
	{
		ITreeBuilder<KVValue, KVLabel> tree = new TreeBuilder<>();

		tree.setRooted(true) //
			.child(KVLabels.create("a")) //
			.child(KVLabels.create("aa")) //
			.endTerminal(KVValues.create(5)) //
			.child(KVLabels.create("ab")) //
			.end().end() //
			.child(KVLabels.create("b")) //
			.endTerminal();

		IBUFTA<KVValue, KVLabel> bufta = new BUBuilder<>(tree).create();

		assertEquals(true, bufta.matcher(tree).matches());

		tree.child(KVLabels.create("c")).end();
		assertEquals(true, bufta.matcher(tree).matches());

		tree.setRooted(false);
		assertEquals(false, bufta.matcher(tree).matches());

		tree.reset().setRooted(true) //
			.child(KVLabels.create("a")) //
			.child(KVLabels.create("aa")) //
			.endTerminal(KVValues.create(5)) //
			.child(KVLabels.create("ab")) //
			.end().end() //
			.child(KVLabels.create("b")) //
			.end();
		assertEquals(false, bufta.matcher(tree).matches());

		tree.reset().setRooted(true) //
			.child(KVLabels.create("a")) //
			.child(KVLabels.create("aa")) //
			.endTerminal(KVValues.create(5)) //
			.child(KVLabels.create("ab")) //
			.end().end() //
			.child(KVLabels.create("b")) //
			.endTerminal(KVValues.create(50));
		assertEquals(true, bufta.matcher(tree).matches());
	}

	@Test
	void find()
	{
		ITreeBuilder<Number, String> builder = new TreeBuilder<>();
		ITree<Number, String>        pattern, element;

//		builder.setRooted(true) //
//			.child("a").end().child("b").endTerminal(5) //
//			.child("x") //
//			.child("a").end().child("b").end().end() //
//			.child("z").child("a").end() //
//			.child("b") //
//		;
		builder.setRooted(true) //
			.child("a").end().child("b").endTerminal(5) //
			.child("x") //
			.child("a").end().child("b").child("c").endTerminal().end().end() //
			.child("z") //
			.child("a").end() //
			.child("b") //
			.child("c") //
			.child("d").end().child("e") //
		;
//		builder.setRooted(true) //
//			.child("z"). //
//			child("a").end() //
//			.child("b").end() //
//			.child("b").end() //
//			.child("a").endTerminal(1) //
//		;
		element = Trees.create(builder);

		builder.reset().setRooted(false) //
			.child("a").end().child("b");
		pattern = Trees.create(builder);

		System.out.println(element);

		IBUFTA<Number, String>       bufta   = new BUBuilder<>(pattern).create();
		ITreeMatcher<Number, String> matcher = bufta.matcher(element);

		System.out.println(TreeOp.bottomUpOrder(element));

		int i = 0;

		while (matcher.find())
		{
			i++;
			System.out.println(matcher.group());
//			assertEquals(pattern, matcher.group());
		}
		assertEquals(3, i);
	}
}