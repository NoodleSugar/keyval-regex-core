package insomnia.implem.fsa.fta.buftachunk.modifier;

import java.util.List;

import insomnia.implem.fsa.fta.buftachunk.BUFTAChunk;

public final class BUFTAListOfModifiers<VAL, LBL> implements IBUFTAChunkModifier<VAL, LBL>
{
	private List<IBUFTAChunkModifier<VAL, LBL>> modifiers;

	private BUFTAListOfModifiers(List<IBUFTAChunkModifier<VAL, LBL>> modifiers)
	{
		this.modifiers = modifiers;
	}

	@Override
	public void accept(BUFTAChunk<VAL, LBL> automaton, Environment<VAL, LBL> env)
	{
		for (var m : modifiers)
			m.accept(automaton, env);
	}

	// ==========================================================================

	@SafeVarargs
	static public <VAL, LBL> BUFTAListOfModifiers<VAL, LBL> of(IBUFTAChunkModifier<VAL, LBL>... modifiers)
	{
		return new BUFTAListOfModifiers<>(List.of(modifiers));
	}
}
