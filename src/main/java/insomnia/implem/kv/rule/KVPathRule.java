package insomnia.implem.kv.rule;

import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Pattern;

import insomnia.implem.kv.data.KVLabel;
import insomnia.implem.kv.data.KVPath;
import insomnia.implem.kv.data.KVPaths;
import insomnia.implem.kv.data.KVValue;
import insomnia.rule.IPathRule;

public class KVPathRule implements IPathRule<KVValue, KVLabel>
{
	public final static String separator = Pattern.quote(".");

//	private KVPath context;
//	private KVPath foot;
	private KVPath body;
	private KVPath head;

	private boolean isExistential;

	public static KVPathRule create(KVPath body, KVPath head)
	{
		return create(body, head, false);
	}

	public static KVPathRule create(KVPath body, KVPath head, boolean isExistential)
	{
		return new KVPathRule(body, head, isExistential);
	}

	public static KVPathRule create(String body, String head)
	{
		return create(body, head, false);
	}

	public static KVPathRule create(String body, String head, boolean isExistential)
	{
		return create(KVPaths.pathFromString(body), KVPaths.pathFromString(head), isExistential);
	}

	private KVPathRule(KVPath body, KVPath head, boolean isExistential) throws IllegalArgumentException
	{
		if (body.isRooted() != head.isRooted())
			throw new IllegalArgumentException("body and head must have the same isRooted value");

		if (!isExistential && body.isTerminal() != head.isTerminal())
			throw new IllegalArgumentException("body and head must have the same isTerminal value for non existential rule");

//		context = null;
//		foot = null;

		this.body          = body;
		this.head          = head;
		this.isExistential = isExistential;
	}

//	@Override
//	public KVPath getContext()
//	{
//		return context;
//	}
//
//	@Override
//	public KVPath getFoot()
//	{
//		return foot;
//	}

	@Override
	public KVPath getBody()
	{
		return body;
	}

	@Override
	public KVPath getHead()
	{
		return head;
	}

	@Override
	public boolean isRooted()
	{
		return body.isRooted();
	}

	@Override
	public boolean isTerminal()
	{
		return body.isTerminal();
	}

	@Override
	public boolean isExistential()
	{
		return isExistential;
	}

	@Override
	public Collection<KVLabel> getVocabulary()
	{
		Collection<KVLabel> ret = new HashSet<>();
		ret.addAll(body.getVocabulary());
		ret.addAll(head.getVocabulary());
		return ret;
	}

	@Override
	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append(body).append(" -> ");

		if (isExistential)
			buffer.append("∃");

		buffer.append(head);
		return buffer.toString();
	}
}
