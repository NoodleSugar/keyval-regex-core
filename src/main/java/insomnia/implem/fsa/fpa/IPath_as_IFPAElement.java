package insomnia.implem.fsa.fpa;

import java.util.List;
import java.util.Optional;

import insomnia.data.IPath;
import insomnia.fsa.fpa.IFPAPath;

public class IPath_as_IFPAElement<VAL, LBL> implements IFPAPath<VAL, LBL>
{
	private final IPath<VAL, LBL> path;

	private IPath_as_IFPAElement(IPath<VAL, LBL> path)
	{
		this.path = path;
	}

	public static <VAL, LBL> IPath_as_IFPAElement<VAL, LBL> get(IPath<VAL, LBL> path)
	{
		return new IPath_as_IFPAElement<VAL, LBL>(path);
	}

	@Override
	public boolean isRooted()
	{
		return path.isRooted();
	}

	@Override
	public boolean isTerminal()
	{
		return path.isTerminal();
	}

	@Override
	public List<LBL> getLabels()
	{
		return path.getLabels();
	}

	@Override
	public Optional<VAL> getValue()
	{
		return path.getValue();
	}
}
