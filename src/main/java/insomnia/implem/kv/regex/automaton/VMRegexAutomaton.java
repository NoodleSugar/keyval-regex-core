package insomnia.implem.kv.regex.automaton;

import java.util.ArrayList;
import java.util.List;

import insomnia.FSA.FSAException;
import insomnia.FSA.IFSAutomaton;
import insomnia.implem.kv.regex.automaton.VMRegexAutomatonBuilder.InstructionData;

public class VMRegexAutomaton implements IFSAutomaton<String>
{
	protected enum Type
	{
		KEY, REGEX, MATCH, JUMP, SPLIT;
	}

	protected class Instruction
	{
		public Type type;
		public String str;

		public Instruction inst1;
		public Instruction inst2;

		{
			str = null;
			inst1 = null;
			inst2 = null;
		}
	}

	List<Instruction> instructions;

	protected VMRegexAutomaton(VMRegexAutomatonBuilder builder)
	{
		int n = builder.instructions.size();
		instructions = new ArrayList<>();

		for(int i = 0; i < n; i++)
			instructions.add(new Instruction());

		for(int i = 0; i < n; i++)
		{
			Instruction inst = instructions.get(i);
			InstructionData data = builder.instructions.get(i);

			inst.type = data.type;
			switch(data.type)
			{
			case KEY:
			case REGEX:
				inst.str = data.str;
				inst.inst1 = instructions.get(data.inst1);
				break;
			case JUMP:
				inst.inst1 = instructions.get(data.inst1);
				break;
			case SPLIT:
				inst.inst1 = instructions.get(data.inst1);
				inst.inst2 = instructions.get(data.inst2);
				break;
			case MATCH:
				break;
			}
		}
	}

	@Override
	public boolean test(List<String> elements) throws FSAException
	{
		return run(elements, instructions.get(0), 0);
	}

	private boolean run(List<String> elements, Instruction inst, int index)
	{
		switch(inst.type)
		{
		case KEY:
			return index < elements.size() && //
					elements.get(index).equals(inst.str) && //
					run(elements, inst.inst1, index + 1);
		case REGEX:
			return index < elements.size() && //
					elements.get(index).matches(inst.str) && //
					run(elements, inst.inst1, index + 1);
		case MATCH:
			return index == elements.size();
		case JUMP:
			return run(elements, inst.inst1, index);
		case SPLIT:
			return run(elements, inst.inst1, index) || //
					run(elements, inst.inst2, index);
		}
		return false;
	}

	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		for(int i = 0; i < instructions.size(); i++)
		{
			Instruction inst = instructions.get(i);
			buf.append(i);
			switch(inst.type)
			{
			case KEY:
				buf.append(" : KEY ");
				buf.append(inst.str);
				buf.append('\n');
				break;
			case REGEX:
				buf.append(" : REGEX ");
				buf.append(inst.str);
				buf.append('\n');
				break;
			case MATCH:
				buf.append(" : MATCH\n");
				break;
			case JUMP:
				buf.append(" : JUMP ");
				buf.append(instructions.indexOf(inst.inst1));
				buf.append('\n');
				break;
			case SPLIT:
				buf.append(" : SPLIT ");
				buf.append(instructions.indexOf(inst.inst1));
				buf.append('|');
				buf.append(instructions.indexOf(inst.inst2));
				buf.append('\n');
				break;
			default:
				buf.append('\n');
				break;
			}
		}
		return buf.toString();
	}
}
