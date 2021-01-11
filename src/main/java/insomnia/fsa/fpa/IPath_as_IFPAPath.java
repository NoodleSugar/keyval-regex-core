package insomnia.fsa.fpa;

import java.util.List;
import java.util.Optional;

import insomnia.data.IPath;

public class IPath_as_IFPAPath<VAL, LBL> implements IFPAPath<VAL, LBL>
{
	private final IPath<VAL, LBL> path;

	private IPath_as_IFPAPath(IPath<VAL, LBL> path)
	{
		this.path = path;
	}

	public static <VAL, LBL> IPath_as_IFPAPath<VAL, LBL> get(IPath<VAL, LBL> path)
	{
		return new IPath_as_IFPAPath<VAL, LBL>(path);
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
