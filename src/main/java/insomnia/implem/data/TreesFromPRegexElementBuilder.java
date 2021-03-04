package insomnia.implem.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.IteratorUtils;

import insomnia.data.ITree;
import insomnia.data.creational.ITreeBuilder;
import insomnia.implem.data.creational.TreeBuilder;
import insomnia.implem.data.regex.parser.IPRegexElement;

final class TreesFromPRegexElementBuilder<VAL, LBL> implements Iterable<ITree<VAL, LBL>>
{
	Function<String, VAL> mapValue;
	Function<String, LBL> mapLabel;

	List<LBL>              labels = new ArrayList<>();
	List<VAL>              values = new ArrayList<>();
	private IPRegexElement element;

	public TreesFromPRegexElementBuilder(IPRegexElement element, Function<String, VAL> mapValue, Function<String, LBL> mapLabel)
	{
		this.mapLabel = mapLabel;
		this.mapValue = mapValue;
		this.element  = element;
		long size = element.longSize();

		if (-1 == size)
			throw new IllegalArgumentException(String.format("Can't handle the infinity size of %s", element));
	}

	public void resetElement(IPRegexElement element)
	{
		this.element = element;
	}

	@Override
	public Iterator<ITree<VAL, LBL>> iterator()
	{
		return new Iterator<ITree<VAL, LBL>>()
		{
			class Data
			{
				IPRegexElement element;

				List<Data> datas;
				List<Data> seqDatas;

				int nbRepeat, pos;

				Data()
				{
				}

				Data(IPRegexElement element)
				{
					this.element  = element;
					this.nbRepeat = element.getQuantifier().getInf();
					seqDatas      = Collections.emptyList();

					switch (element.getType())
					{
					case KEY:
					case EMPTY:
						datas = Collections.emptyList();
						break;
					case DISJUNCTION:
					case SEQUENCE:
					case NODE:
						datas = new ArrayList<>();

						for (IPRegexElement e : element.getElements())
							datas.add(new Data(e));

						initSeqDatas(element.getQuantifier().getSup());
						break;
					default:
						throw new UnsupportedOperationException(element.getType().toString());
					}
				}

				Data(Data src)
				{
					element  = src.element;
					nbRepeat = element.getQuantifier().getInf();
					datas    = src.datas.stream().map(d -> new Data(d)).collect(Collectors.toList());
					seqDatas = src.seqDatas.stream().map(d -> new Data(d)).collect(Collectors.toList());
				}

				private Data seqData(Data src)
				{
					Data ret = new Data();
					ret.element  = src.element;
					ret.nbRepeat = 1;                                                                    //element.getQuantifier().getInf();
					ret.datas    = src.datas.stream().map(d -> new Data(d)).collect(Collectors.toList());
					ret.seqDatas = Collections.emptyList();
					return ret;
				}

				void initSeqDatas(int nb)
				{
					if (nb == 0)
						return;
					if (nb == 1)
						return;

					seqDatas = new ArrayList<>();

					while (nb-- != 0)
						seqDatas.add(seqData(this));
					this.datas = Collections.emptyList();
				}

				void reset()
				{
					nbRepeat = element.getQuantifier().getInf();
					pos      = 0;
					for (Data d : datas)
						d.reset();
					for (Data d : seqDatas)
						d.reset();
				}

				@Override
				public String toString()
				{
					return new StringBuffer().append("@").append(pos) //
						.append("+").append(nbRepeat) //
						.append(":").append(element).toString();
				}
			} // End of Data

			ITreeBuilder<VAL, LBL> treeBuilder;
			Data                   data;

			{
				data        = new Data(element);
				treeBuilder = new TreeBuilder<>();
				makeTree(data);
			}

			@Override
			public boolean hasNext()
			{
				return data != null;
			}

			public void makeTree(Data data)
			{
				IPRegexElement e        = data.element;
				int            nbRepeat = data.nbRepeat;

				switch (e.getType())
				{
				case EMPTY:
				{
					VAL value = mapValue.apply(e.getValue());
					treeBuilder.setValue(value);

					if (e.isTerminal())
						treeBuilder.setTerminal(true);
					if (e.isRooted() && treeBuilder.isEmpty())
						treeBuilder.setRooted(true);
					break;
				}
				case KEY:
				{
					assert (!e.isRooted());

					VAL value = mapValue.apply(e.getValue());
					LBL label = mapLabel.apply(e.getLabel());

					if (treeBuilder.getCurrentNode().isTerminal())
						treeBuilder.setTerminal(false);

					while (nbRepeat-- > 0)
						treeBuilder.addChildDown(label, value);

					if (e.isTerminal())
						treeBuilder.setTerminal(true);
					break;
				}
				case DISJUNCTION:
					if (data.seqDatas.isEmpty())
						makeTree(data.datas.get(data.pos));
					else
						while (nbRepeat-- > 0)
							makeTree(data.seqDatas.get(nbRepeat));
					break;
				case NODE:
					if (data.seqDatas.isEmpty())
					{
						int coordinates[] = treeBuilder.getCurrentCoordinates();
						for (Data d : data.datas)
						{
							makeTree(d);
							treeBuilder.setCurrentCoordinates(coordinates);
						}
					}
					else
					{
						while (nbRepeat-- > 0)
							makeTree(data.seqDatas.get(nbRepeat));
					}
					break;
				case SEQUENCE:
					if (data.seqDatas.isEmpty())
						for (Data d : data.datas)
							makeTree(d);
					else
						while (nbRepeat-- > 0)
							makeTree(data.seqDatas.get(nbRepeat));
					break;
				default:
					throw new UnsupportedOperationException(e.getType().toString());
				}
			}

			@Override
			public ITree<VAL, LBL> next()
			{
				if (!hasNext())
					return null;

				ITree<VAL, LBL> ret = Trees.create(treeBuilder);

				if (false == forward(data))
				{
					this.data = null;
					return ret;
				}
				treeBuilder.reset();
				makeTree(data);
				return ret;
			}

			private boolean forwardNb(List<Data> datas, int nb)
			{
				ListIterator<Data> it = datas.listIterator();
				for (;; nb--)
				{
					if (!it.hasNext() || nb == 0)
						return false;
					if (forward(it.next()))
						break;
				}
				it.previous();
				while (it.hasPrevious())
					it.previous().reset();
				return true;
			}

			private boolean forwardOneDisjunctionData(Data data)
			{
				ListIterator<Data> it = data.datas.listIterator();

				if (data.pos > 0)
					IteratorUtils.get(it, data.pos - 1);

				if (!it.hasNext())
					return false;
				if (forward(it.next()))
					return true;

				data.pos++;
				return data.pos != data.element.getElements().size();
			}

			private boolean forwardOneSeqData(Data data)
			{
				ListIterator<Data> it = data.datas.listIterator();
				for (;;)
				{
					if (!it.hasNext())
						return false;
					if (forward(it.next()))
						break;
				}
				it.previous();
				while (it.hasPrevious())
					it.previous().reset();
				return true;
			}

			private boolean forward(Data data)
			{
				switch (data.element.getType())
				{
				case KEY:
					if (data.nbRepeat == data.element.getQuantifier().getSup())
						return false;

					data.nbRepeat++;
					return true;
				case DISJUNCTION:
				{
					if (!data.seqDatas.isEmpty())
					{
						if (forwardNb(data.seqDatas, data.nbRepeat))
							return true;

						if (data.nbRepeat == data.element.getQuantifier().getSup())
							return false;

						data.nbRepeat++;
						data.seqDatas.forEach(d -> d.reset());
						return true;
					}
					else
						return forwardOneDisjunctionData(data);
				}
				case SEQUENCE:
				case NODE:
				{
					if (!data.seqDatas.isEmpty())
					{
						if (forwardNb(data.seqDatas, data.nbRepeat))
							return true;

						if (data.nbRepeat == data.element.getQuantifier().getSup())
							return false;

						data.nbRepeat++;
						data.seqDatas.forEach(d -> d.reset());
						return true;
					}
					else
						return forwardOneSeqData(data);
				}
				case EMPTY:
					return false;
				default:
					throw new UnsupportedOperationException(data.element.getType().toString());
				}
			}
		};
	}
}
