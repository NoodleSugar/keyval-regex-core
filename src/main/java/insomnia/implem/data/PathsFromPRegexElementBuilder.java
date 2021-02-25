package insomnia.implem.data;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Function;

import insomnia.data.IPath;
import insomnia.implem.data.creational.PathBuilder;
import insomnia.implem.data.regex.parser.IPRegexElement;

final class PathsFromPRegexElementBuilder<VAL, LBL> implements Iterable<IPath<VAL, LBL>>
{
	Function<String, VAL> mapValue;
	Function<String, LBL> mapLabel;

	List<LBL>              labels = new ArrayList<>();
	List<VAL>              values = new ArrayList<>();
	private IPRegexElement element;

	public PathsFromPRegexElementBuilder(IPRegexElement element, Function<String, VAL> mapValue, Function<String, LBL> mapLabel)
	{
		this.mapLabel = mapLabel;
		this.mapValue = mapValue;
		this.element  = element;
	}

	public void resetElement(IPRegexElement element)
	{
		this.element = element;
	}

	@Override
	public Iterator<IPath<VAL, LBL>> iterator()
	{
		return new Iterator<IPath<VAL, LBL>>()
		{
			class Data
			{
				List<Data>     datas;
				int            pos;
				int            repeated;
				IPRegexElement element;

				Data(IPRegexElement element)
				{
					if (element.getQuantifier().getSup() == -1)
						throw new InvalidParameterException("Can't handle infinite quantifier");

					this.pos      = 0;
					this.repeated = element.getQuantifier().getInf();

					this.element = element;
					switch (element.getType())
					{
					case KEY:
					case EMPTY:
						datas = Collections.emptyList();
						break;
					case DISJUNCTION:
					case SEQUENCE:
						datas = new ArrayList<>();

						for (IPRegexElement e : element.getElements())
							datas.add(new Data(e));
						break;
					default:
						throw new InvalidParameterException();
					}
				}

				void reset()
				{
					pos      = 0;
					repeated = element.getQuantifier().getInf();

					for (Data d : datas)
						d.reset();
				}

				@Override
				public String toString()
				{
					return pos + ":" + element;
				}
			}

			PathBuilder<VAL, LBL> pathBuilder;
			Data                  data;

			{
				data        = new Data(element);
				pathBuilder = new PathBuilder<>();
				makePath(data);
			}

			@Override
			public boolean hasNext()
			{
				return data != null;
			}

			public void makePath(Data data)
			{
				IPRegexElement e        = data.element;
				int            repeated = data.repeated;

				switch (e.getType())
				{
				case EMPTY:
				{
					VAL value = mapValue.apply(e.getValue());
					pathBuilder.setValue(value);

					if (e.isTerminal())
						pathBuilder.setTerminal(true);
					if (e.isRooted())
						pathBuilder.setRooted(true);
					break;
				}
				case KEY:
				{
					assert (!e.isRooted());

					VAL value = mapValue.apply(e.getValue());
					LBL label = mapLabel.apply(e.getLabel());
					while (repeated-- > 0)
						pathBuilder.childDown(label, value);

					if (e.isTerminal())
						pathBuilder.setTerminal(true);
					break;
				}
				case DISJUNCTION:
					while (repeated-- > 0)
						makePath(data.datas.get(data.pos));
					break;
				case SEQUENCE:
					while (repeated-- > 0)
						for (Data d : data.datas)
							makePath(d);
					break;
				}
			}

			@Override
			public IPath<VAL, LBL> next()
			{
				if (!hasNext())
					return null;

				IPath<VAL, LBL> ret = Paths.create(pathBuilder);

				if (false == forward(data, true))
				{
					this.data = null;
					return ret;
				}
				pathBuilder.reset();
				makePath(data);
				return ret;
			}

			public boolean forward(Data data, boolean mustChange)
			{
				if (mustChange == false)
					return false;

				switch (data.element.getType())
				{
				case KEY:
					if (data.repeated == data.element.getQuantifier().getSup())
						return false;
					data.repeated++;
					return true;
				case DISJUNCTION:

					if (data.repeated == data.element.getQuantifier().getSup())
					{
						int c = data.element.getElements().size();
						data.repeated = data.element.getQuantifier().getInf();
						if (data.repeated == 0)
							data.repeated++;

						Data d = data.datas.get(data.pos);
						if (forward(d, true))
							return true;

						if (data.pos == c - 1)
							return false;

						data.pos++;
						return true;
					}
					data.repeated++;
					return true;
				case SEQUENCE:
					if (data.repeated == data.element.getQuantifier().getSup())
					{
						data.repeated = data.element.getQuantifier().getInf();

						ListIterator<Data> it = data.datas.listIterator();
						for (;;)
						{
							if (forward(it.next(), true))
								break;
							if (!it.hasNext())
								return false;
						}
						it.previous();
						while (it.hasPrevious())
							it.previous().reset();
					}
					else
						data.repeated++;
					return true;
				default:
					return false;
				}
			}
		};
	}
}
