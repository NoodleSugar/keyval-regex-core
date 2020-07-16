package insomnia.rule;

import insomnia.rule.tree.IPath;
import insomnia.rule.tree.Path;

public class PathRule implements IRule<IPath>
{
	private IPath context;
	private IPath foot;
	private IPath body;
	private IPath head;
	private boolean rooted;
	private boolean valued;
	private boolean existential;

	public PathRule(String b, String h)
	{
		this(b, h, false, false, false);
	}

	public PathRule(String b, String h, boolean isRooted, boolean isValued, boolean isExistential)
	{
		context = null;
		foot = null;
		body = new Path(b, isRooted, isValued);
		head = new Path(h, isRooted, isValued);
		rooted = isRooted;
		valued = isValued;
		existential = isExistential;
	}

	@Override
	public IPath getContext()
	{
		return context;
	}

	@Override
	public IPath getFoot()
	{
		return foot;
	}

	@Override
	public IPath getBody()
	{
		return body;
	}

	@Override
	public IPath getHead()
	{
		return head;
	}

	@Override
	public boolean dependsWeakOn(IRule<IPath> rule)
	{
		IPath head2 = rule.getHead();

		// Si R2 est existentielle
		// Si R1 est valuée
		// Si R2 n'est pas valuée
		if(rule.isExistential() && valued && !rule.isValued())
			return false;

		// Si R2 est enracinée
		if(rule.isRooted())
		{
			// Si R2 est valuée
			if(rule.isValued())
				// Si h2 est égal à b1
				return head2.getKeys().equals(body.getKeys());

			// Si R2 non valuée
			else
				// Si un suffixe de h2 est préfixe de b1
				return body.hasPrefixInSuffix(head2);
		}

		// Si R2 non enracinée
		else
		{
			// Si R2 est valuée
			if(rule.isValued())
				// Si un préfixe de h2 est suffixe de b1
				return head2.hasPrefixInSuffix(body);

			// Si R2 non valuée
			else
				// Si h2 est inclu dans b1 OU
				// Si un préfixe de h2 est suffixe de b1 OU
				// Si un suffixe de h2 est préfixe de b1
				return head2.isIncluded(body) || head2.hasPrefixInSuffix(body) || body.hasPrefixInSuffix(head2);
		}

	}

	@Override
	public boolean dependsStrongOn(IRule<IPath> rule)
	{
		IPath head2 = rule.getHead();

		// Si R2 est existentielle
		// Si R1 est valuée
		// Si R2 n'est pas valuée
		if(rule.isExistential() && valued && !rule.isValued())
			return false;

		// Si R1 est enracinée
		if(rooted)
		{
			// Si R1 est valuée
			if(valued)
				// Si b1 est égal à h2
				return body.getKeys().equals(head2.getKeys());

			// Si R1 non valuée
			else
				// Si b1 est préfixe de h2
				return body.isPrefix(head2);
		}

		// Si R1 non enracinée
		else
		{
			// Si R1 est valuée
			if(valued)
				// Si b1 est suffixe de h2
				return body.isSuffix(head2);

			// Si R1 non valuée
			else
				// Si b1 est inclu dans h2
				return body.isIncluded(head2);
		}
	}

	@Override
	public boolean isRooted()
	{
		return rooted;
	}

	@Override
	public boolean isValued()
	{
		return valued;
	}

	@Override
	public boolean isExistential()
	{
		return existential;
	}
}
