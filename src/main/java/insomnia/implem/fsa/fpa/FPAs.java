package insomnia.implem.fsa.fpa;

import insomnia.fsa.fpa.AbstractSimpleGFPA;
import insomnia.fsa.fpa.IGFPA;

public final class FPAs<VAL, LBL> //
{
	private FPAs()
	{
		throw new AssertionError();
	}

	// =========================================================================

	private static abstract class AbstractFSA<VAL, LBL> extends AbstractSimpleGFPA<VAL, LBL>
	{
		AbstractFSA(IGFPA<VAL, LBL> graphFPA)
		{
			super(graphFPA.getStates(), graphFPA.getRootedStates(), graphFPA.getTerminalStates(), graphFPA.getInitialStates(), graphFPA.getFinalStates(), graphFPA.getAllEdges(), graphFPA.getProperties());
		}
	}

	private static class FSASync<VAL, LBL> extends AbstractFSA<VAL, LBL>
	{
		FSASync(IGFPA<VAL, LBL> graphFPABuilder)
		{
			super(graphFPABuilder);
		}
	}

	private static class FSAGeneral<VAL, LBL> extends AbstractFSA<VAL, LBL>
	{
		FSAGeneral(IGFPA<VAL, LBL> graphFPABuilder)
		{
			super(graphFPABuilder);
		}
	}

	// =========================================================================

	public static <VAL, LBL> IGFPA<VAL, LBL> create(IGFPA<VAL, LBL> graphFPABuilder)
	{
		if (graphFPABuilder.getProperties().isSynchronous())
			return new FSASync<VAL, LBL>(graphFPABuilder);
		else
			return new FSAGeneral<VAL, LBL>(graphFPABuilder);
	}
}