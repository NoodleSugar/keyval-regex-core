package insomnia.implem.fsa.fta;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import insomnia.data.factory.ITreeBuilder;
import insomnia.fsa.fta.IBUFTA;
import insomnia.implem.data.factory.TreeBuilder;
import insomnia.implem.fsa.fta.bubuilder.BUBuilder;
import insomnia.implem.kv.data.KVLabel;
import insomnia.implem.kv.data.KVLabels;
import insomnia.implem.kv.data.KVValue;
import insomnia.implem.kv.data.KVValues;

public class TestAutomaton
{
	// =========================================================================

	@Test
	void match()
	{
		ITreeBuilder<KVValue, KVLabel> tree = new TreeBuilder<>();

		tree.setRooted(true) //
			.add(KVLabels.create("a")) //
			.add(KVLabels.create("aa")) //
			.endTerminal(KVValues.create(5)) //
			.add(KVLabels.create("ab")) //
			.end().end() //
			.add(KVLabels.create("b")) //
			.endTerminal();

		IBUFTA<KVValue, KVLabel> bufta = new BUBuilder<>(tree).build();

		assertEquals(true, bufta.test(tree));

		tree.add(KVLabels.create("c")).end();
		assertEquals(true, bufta.test(tree));

		tree.setRooted(false);
		assertEquals(false, bufta.test(tree));

		tree.clear().setRooted(true) //
			.add(KVLabels.create("a")) //
			.add(KVLabels.create("aa")) //
			.endTerminal(KVValues.create(5)) //
			.add(KVLabels.create("ab")) //
			.end().end() //
			.add(KVLabels.create("b")) //
			.end();
		assertEquals(false, bufta.test(tree));

		tree.clear().setRooted(true) //
			.add(KVLabels.create("a")) //
			.add(KVLabels.create("aa")) //
			.endTerminal(KVValues.create(5)) //
			.add(KVLabels.create("ab")) //
			.end().end() //
			.add(KVLabels.create("b")) //
			.endTerminal(KVValues.create(50));
		assertEquals(true, bufta.test(tree));
	}
}