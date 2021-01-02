package insomnia.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;

public abstract class AbstractPath<VAL, LBL> implements IPath<VAL, LBL>
{
	public final static boolean default_isRoot     = false;
	public final static boolean default_isTerminal = false;

	private List<LBL> labels;
	private VAL       value;

	private boolean isRooted;
	private boolean isTerminal;

	private VAL nullValue()
	{
		return null;
	}

	@SuppressWarnings("unchecked")
	public AbstractPath(IPath<VAL, LBL> path, int begin, int end)
	{
		if (begin == end)
		{
			initPath(this, default_isRoot, default_isTerminal, Collections.EMPTY_LIST, nullValue());
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
			value = path.getValue().orElse(null);
		else
			value = nullValue();

		initPath(this, isRooted, isTerminal, path.getLabels().subList(begin, end), value);
	}

	public AbstractPath(List<? extends LBL> labels)
	{
		initPath(this, default_isRoot, default_isTerminal, labels, nullValue());
	}

	public AbstractPath(List<? extends LBL> labels, VAL value)
	{
		initPath(this, default_isRoot, default_isTerminal, labels, value);
	}

	public AbstractPath(boolean isRooted, List<? extends LBL> labels)
	{
		initPath(this, isRooted, default_isTerminal, labels, nullValue());
	}

	public AbstractPath(boolean isRooted, List<? extends LBL> labels, VAL value)
	{
		initPath(this, isRooted, default_isTerminal, labels, value);
	}

	public AbstractPath(boolean isRooted, boolean isTerminal, List<? extends LBL> labels)
	{
		initPath(this, isRooted, isTerminal, labels, nullValue());
	}

	public AbstractPath(boolean isRooted, boolean isTerminal, List<? extends LBL> labels, VAL value)
	{
		initPath(this, isRooted, isTerminal, labels, value);
	}

	@Override
	public Optional<IEdge<VAL, LBL>> getChild(INode<VAL, LBL> node)
	{
		List<? extends IEdge<VAL, LBL>> childs = getChildren(node);

		if (childs.isEmpty())
			return Optional.empty();

		return Optional.of(childs.get(0));
	}

	private static <VAL, LBL> void initPath(AbstractPath<VAL, LBL> path, boolean isRooted, boolean isTerminal, List<? extends LBL> labels, VAL value)
	{
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
		return Collections.unmodifiableList(labels);
	}

	@Override
	public Collection<LBL> getVocabulary()
	{
		return new HashSet<>(labels);
	}

	@Override
	public Optional<VAL> getValue()
	{
		return Optional.ofNullable(value);
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

		if (getValue().isPresent())
			buf.append(getValue().get());

		return buf.toString();
	}
}
