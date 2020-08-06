package insomnia.regex.automaton;

import java.util.List;

import insomnia.automaton.AutomatonException;
import insomnia.automaton.IAutomaton;

public class VMRegexAutomaton implements IAutomaton<String>
{
	protected enum Type
	{
		KEY, REGEX, MATCH, JUMP, SPLIT;
	}

	protected class Instruction
	{
		public Type type;
		public String key;

		public Instruction inst1;
		public Instruction inst2;

		public Instruction(Type t, String k, Instruction i1, Instruction i2)
		{
			type = t;
			key = k;
			inst1 = i1;
			inst2 = i2;
		}
	}

	private Instruction root;
	
	protected VMRegexAutomaton(VMRegexAutomatonBuilder builder)
	{
		
	}

	@Override
	public boolean run(List<String> elements) throws AutomatonException
	{
		return run(elements, root, 0);
	}

	private boolean run(List<String> elements, Instruction inst, int index)
	{
		switch(inst.type)
		{
		case KEY:
			return inst.key.equals(elements.get(index)) && //
					run(elements, inst.inst1, index + 1);
		case MATCH:
			return true;
		case JUMP:
			return run(elements, inst.inst1, index);
		case SPLIT:
			return run(elements, inst.inst1, index) || //
					run(elements, inst.inst2, index);
		}
		return false;
	}
}
