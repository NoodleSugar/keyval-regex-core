package insomnia.fsa.fta;

import insomnia.data.ITree;
import insomnia.data.regex.ITreeMatcher;

public interface IFTA<VAL, LBL>
{
	ITreeMatcher<VAL, LBL> matcher(ITree<VAL, LBL> element);
}
