package insomnia.regex.automaton;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import insomnia.regex.automaton.RegexAutomatonBuilder.BuilderException;
import insomnia.regex.automaton.VMRegexAutomaton.Type;
import insomnia.regex.element.IElement;
import insomnia.regex.element.Key;
import insomnia.regex.element.MultipleElement;
import insomnia.regex.element.OrElement;
import insomnia.regex.element.Quantifier;
import insomnia.regex.element.Regex;

public class VMRegexAutomatonBuilder
{
	protected class InstructionData
	{
		public Type type;
		public String str;

		public int inst1;
		public int inst2;

		public InstructionData(Type t, String s, int i1, int i2)
		{
			type = t;
			str = s;
			inst1 = i1;
			inst2 = i2;
		}
	}

	List<InstructionData> instructions;

	public VMRegexAutomatonBuilder(IElement elements) throws BuilderException
	{
		instructions = new ArrayList<>();
		recursiveConstruct(elements, 0, true);
	}

	private int recursiveConstruct(IElement element, int current, boolean matching) throws BuilderException
	{
		Quantifier q = element.getQuantifier();
		int inf = q.getInf();
		int sup = q.getSup();

		for(int i = 0; i < inf; i++)
			current = subRecursiveConstruct(element, current, matching);
		if(sup == -1)
		{
			InstructionData split = //
					new InstructionData(Type.SPLIT, null, ++current, 0);
			instructions.add(split);
			int next = subRecursiveConstruct(element, current, matching);
			instructions.add(split);

			split.inst2 = ++next;
			current = next;
		}
		else
		{
			ArrayList<InstructionData> splits = new ArrayList<>();
			for(int i = 0; i < sup - inf; i++)
			{
				InstructionData split = //
						new InstructionData(Type.SPLIT, null, ++current, 0);
				instructions.add(split);
				splits.add(split);
				current = subRecursiveConstruct(element, current, matching);
			}
			for(InstructionData split : splits)
				split.inst2 = current;
		}

		return current;
	}

	private int subRecursiveConstruct(IElement element, int current, boolean matching) throws BuilderException
	{
		if(element instanceof Key)
		{
			Key key = (Key) element;
			instructions.add(new InstructionData(Type.KEY, key.getLabel(), ++current, 0));
			if(matching)
				instructions.add(new InstructionData(Type.MATCH, null, ++current, 0));
		}
		else if(element instanceof Regex)
		{
			Regex regex = (Regex) element;
			instructions.add(new InstructionData(Type.REGEX, regex.getRegex(), ++current, 0));
			if(matching)
				instructions.add(new InstructionData(Type.MATCH, null, ++current, 0));
		}
		else if(element instanceof OrElement)
		{
			OrElement oe = (OrElement) element;
			int n = oe.size();
			IElement[] elts = new IElement[n];
			elts = oe.toArray(elts);

			ArrayList<InstructionData> jumps = new ArrayList<>();
			for(int i = 0; i < n - 1; i++)
			{
				InstructionData split, jump;
				split = new InstructionData(Type.SPLIT, null, ++current, 0);
				instructions.add(split);

				current = recursiveConstruct(elts[i], current, false);
				if(matching)
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
			for(InstructionData jump : jumps)
				jump.inst1 = current;
			if(matching)
				instructions.add(new InstructionData(Type.MATCH, null, ++current, 0));
		}
		else if(element instanceof MultipleElement)
		{
			MultipleElement me = (MultipleElement) element;
			for(IElement e : me)
				current = recursiveConstruct(e, current, false);
			if(matching)
				instructions.add(new InstructionData(Type.MATCH, null, ++current, 0));
		}
		else
			throw new InvalidParameterException("Invalid type for parameter");

		return current;
	}

	public VMRegexAutomaton build()
	{
		return new VMRegexAutomaton(this);
	}
}
