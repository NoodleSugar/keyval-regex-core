package insomnia.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.BooleanUtils;

public abstract class AbstractPath<VAL, LBL> implements IPath<VAL, LBL>
{
	public final static boolean default_isRoot     = false;
	public final static boolean default_isTerminal = false;

	private List<LBL> labels;
	private VAL       value;

	private boolean isRooted;
	private boolean isTerminal;

	public abstract VAL emptyValue();

	@SuppressWarnings("unchecked")
	public AbstractPath(IPath<VAL, LBL> path, int begin, int end)
	{
		if (begin == end)
		{
			initPath(this, default_isRoot, default_isTerminal, Collections.EMPTY_LIST, emptyValue());
			return;
		}
		boolean isRooted   = false;
		boolean isTerminal = false;
		VAL     value;

		if (path.isRooted())
		{
			if (begin > 0)
			{
				begin--;
				end -= 2;
			}
			else
			{
				isRooted = true;
				end--;
			}
		}

		if (path.isTerminal())
		{
			if (end == path.nbLabels() + 1)
			{
				isTerminal = true;
				end--;
			}
		}

		if (isTerminal)
			value = path.getValue();
		else
			value = emptyValue();

		initPath(this, isRooted, isTerminal, path.getLabels().subList(begin, end), value);
	}

	public AbstractPath(List<? extends LBL> labels)
	{
		initPath(this, default_isRoot, default_isTerminal, labels, emptyValue());
	}

	public AbstractPath(List<? extends LBL> labels, VAL value)
	{
		initPath(this, default_isRoot, default_isTerminal, labels, value);
	}

	public AbstractPath(boolean isRooted, List<? extends LBL> labels)
	{
		initPath(this, isRooted, default_isTerminal, labels, emptyValue());
	}

	public AbstractPath(boolean isRooted, List<? extends LBL> labels, VAL value)
	{
		initPath(this, isRooted, default_isTerminal, labels, value);
	}

	public AbstractPath(boolean isRooted, boolean isTerminal, List<? extends LBL> labels)
	{
		initPath(this, isRooted, isTerminal, labels, emptyValue());
	}

	public AbstractPath(boolean isRooted, boolean isTerminal, List<? extends LBL> labels, VAL value)
	{
		initPath(this, isRooted, isTerminal, labels, value);
	}

	private static <VAL, LBL> void initPath(AbstractPath<VAL, LBL> path, boolean isRooted, boolean isTerminal, List<? extends LBL> labels, VAL value)
	{
		assert (value != null);
		path.isRooted   = isRooted;
		path.isTerminal = isTerminal;
		path.value      = value;
		/*
		 * Empty path
		 */
		if (labels.size() == 0)
		{
			path.labels = Collections.emptyList();
			return;
		}
		path.labels = new ArrayList<>(labels);
	}

	public int nbLabels()
	{
		return labels.size();
	}

	@Override
	public boolean isEmpty()
	{
		return labels.isEmpty();
	}

	@Override
	public List<LBL> getLabels()
	{
		return new ArrayList<>(labels);
	}

	@Override
	public Collection<LBL> getVocabulary()
	{
		return new HashSet<>(labels);
	}

	@Override
	public VAL getValue()
	{
		return value;
	}

	@Override
	public int size()
	{
		return labels.size() + BooleanUtils.toInteger(isRooted) + BooleanUtils.toInteger(isTerminal);
	}

	@Override
	public boolean isRooted()
	{
		return isRooted;
	}

	@Override
	public boolean isTerminal()
	{
		return isTerminal;
	}

	@Override
	public boolean isComplete()
	{
		return isRooted() && isTerminal();
	}

	@Override
	public boolean isFree()
	{
		return !isRooted() && !isTerminal();
	}

	@Override
	public boolean isFixed()
	{
		return isRooted() || isTerminal();
	}

	// =========================================================================
	// Object Override

	@Override
	public boolean equals(Object o)
	{
		if (o == null || !(o instanceof IPath))
			return false;

		return PathOp.areEquals(this, (IPath<?, ?>) o) && ((IPath<?, ?>) o).getValue().equals(getValue());
	}

	@Override
	public int hashCode()
	{
		return this.labels.hashCode() + BooleanUtils.toInteger(isRooted) + BooleanUtils.toInteger(isTerminal);
	}

	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append(isRooted ? "^" : "");

		if (labels.size() > 0)
		{
			for (LBL label : labels)
				buf.append(label).append(".");

			if (!isTerminal)
				buf.deleteCharAt(buf.length() - 1);
		}

		if (isTerminal)
			buf.append("$");

		buf.append(getValue());

		return buf.toString();
	}
}
