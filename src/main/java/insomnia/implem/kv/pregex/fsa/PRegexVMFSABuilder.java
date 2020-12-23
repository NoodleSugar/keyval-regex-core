package insomnia.implem.kv.pregex.fsa;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import insomnia.implem.kv.data.KVLabel;
import insomnia.implem.kv.data.KVValue;
import insomnia.implem.kv.pregex.IPRegexElement;
import insomnia.implem.kv.pregex.Quantifier;
import insomnia.implem.kv.pregex.PRegexElements.Disjunction;
import insomnia.implem.kv.pregex.PRegexElements.Key;
import insomnia.implem.kv.pregex.PRegexElements.Regex;
import insomnia.implem.kv.pregex.PRegexElements.Sequence;
import insomnia.implem.kv.pregex.fsa.PRegexVMFSA.Type;

class PRegexVMFSABuilder
{
	protected class InstructionData
	{
		public Type   type;
		public String str;

		public int inst1;
		public int inst2;

		public InstructionData(Type t, String s, int i1, int i2)
		{
			type  = t;
			str   = s;
			inst1 = i1;
			inst2 = i2;
		}
	}

	List<InstructionData> instructions;

	public PRegexVMFSABuilder(IPRegexElement elements)
	{
		instructions = new ArrayList<>();
		recursiveConstruct(elements, 0, true);
	}

	private int recursiveConstruct(IPRegexElement element, int current, boolean matching)
	{
		Quantifier q   = element.getQuantifier();
		int        inf = q.getInf();
		int        sup = q.getSup();

		for (int i = 0; i < inf; i++)
			current = subRecursiveConstruct(element, current, matching);
		if (sup == -1)
		{
			InstructionData split = //
				new InstructionData(Type.SPLIT, null, ++current, 0);
			instructions.add(split);
			int next = subRecursiveConstruct(element, current, matching);
			instructions.add(split);

			split.inst2 = ++next;
			current     = next;
		}
		else
		{
			ArrayList<InstructionData> splits = new ArrayList<>();
			for (int i = 0; i < sup - inf; i++)
			{
				InstructionData split = //
					new InstructionData(Type.SPLIT, null, ++current, 0);
				instructions.add(split);
				splits.add(split);
				current = subRecursiveConstruct(element, current, matching);
			}
			for (InstructionData split : splits)
				split.inst2 = current;
		}

		return current;
	}

	private int subRecursiveConstruct(IPRegexElement element, int current, boolean matching)
	{
		switch (element.getType())
		{

		case KEY:
		{
			Key key = (Key) element;
			instructions.add(new InstructionData(Type.KEY, key.getLabel(), ++current, 0));
			if (matching)
				instructions.add(new InstructionData(Type.MATCH, null, ++current, 0));
		}
			break;

		case REGEX:
		{
			Regex regex = (Regex) element;
			instructions.add(new InstructionData(Type.REGEX, regex.getRegex(), ++current, 0));
			if (matching)
				instructions.add(new InstructionData(Type.MATCH, null, ++current, 0));
		}
			break;

		case DISJUNCTION:
		{
			Disjunction  oe   = (Disjunction) element;
			int        n    = oe.getElements().size();
			IPRegexElement[] elts = new IPRegexElement[n];
			elts = oe.getElements().toArray(elts);

			ArrayList<InstructionData> jumps = new ArrayList<>();
			for (int i = 0; i < n - 1; i++)
			{
				InstructionData split, jump;
				split = new InstructionData(Type.SPLIT, null, ++current, 0);
				instructions.add(split);

				current = recursiveConstruct(elts[i], current, false);
				if (matching)
					instructions.add(new InstructionData(Type.MATCH, null, ++current, 0));
				else
				{
					jump = new InstructionData(Type.JUMP, null, ++current, 0);
					jumps.add(jump);
					instructions.add(jump);
				}

				split.inst2 = current;
			}
			current = recursiveConstruct(elts[n - 1], current, false);

			for (InstructionData jump : jumps)
				jump.inst1 = current;

			if (matching)
				instructions.add(new InstructionData(Type.MATCH, null, ++current, 0));
		}
			break;

		case SEQUENCE:
		{
			Sequence me = (Sequence) element;

			for (IPRegexElement e : me.getElements())
				current = recursiveConstruct(e, current, false);

			if (matching)
				instructions.add(new InstructionData(Type.MATCH, null, ++current, 0));
		}
			break;

		default:
			throw new InvalidParameterException("Invalid type for parameter");
		}
		return current;
	}

	public PRegexVMFSA<KVValue, KVLabel> build()
	{
		return new PRegexVMFSA<>(this);
	}
}
