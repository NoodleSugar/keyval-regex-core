package insomnia.fsa;

import java.util.List;
import java.util.Optional;

import insomnia.data.IPath;

public class IPath_as_IFSAElement<VAL, LBL> implements IFSAElement<VAL, LBL>
{
	private final IPath<VAL, LBL> path;

	private IPath_as_IFSAElement(IPath<VAL, LBL> path)
	{
		this.path = path;
	}

	public static <VAL, LBL> IPath_as_IFSAElement<VAL, LBL> get(IPath<VAL, LBL> path)
	{
		return new IPath_as_IFSAElement<VAL, LBL>(path);
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
