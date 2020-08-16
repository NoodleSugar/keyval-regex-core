package insomnia.rule.ontology;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import insomnia.rule.PathRule;
import insomnia.rule.tree.Path;

public class PathGRD implements IGRD<Path, PathRule>
{
	private class RuleDep
	{
		protected Map<DependencyMode, List<PathRule>> parents;
		protected Map<DependencyMode, List<PathRule>> children;

		public RuleDep()
		{
			parents = new HashMap<>();
			children = new HashMap<>();

			addMode(DependencyMode.STRONG);
			addMode(DependencyMode.WEAK);
		}

		public void addMode(DependencyMode mode)
		{
			parents.put(mode, new ArrayList<>());
			children.put(mode, new ArrayList<>());
		}
	}

	// Dépendances de matérialisation
	Map<PathRule, RuleDep> dependencies;

	public PathGRD(IOntology<PathRule> ontology)
	{
		dependencies = new HashMap<>();

		// Pour chaque régle de l'ontologie
		for(PathRule rule : ontology)
			addRule(rule);
	}

	private void addRule(PathRule rule)
	{
		// Initialisation des dépendances de la règle
		RuleDep dependency = new RuleDep();
		dependencies.put(rule, dependency);

		// Pour chaque règle déja ajoutée au graphe
		for(Entry<PathRule, RuleDep> entry : dependencies.entrySet())
		{
			PathRule rule2 = entry.getKey();
			RuleDep dependency2 = entry.getValue();

			// Vérification des dépendances de la rule2 à rule
			if(dependsWeakOn(rule2, rule))
			{
				dependency.children.get(DependencyMode.WEAK).add(rule2);
				dependency2.parents.get(DependencyMode.WEAK).add(rule);
			}
			if(dependsStrongOn(rule2, rule))
			{
				dependency.children.get(DependencyMode.STRONG).add(rule2);
				dependency2.parents.get(DependencyMode.STRONG).add(rule);
			}

			// Vérification des dépendances de rule à rule2
			if(dependsWeakOn(rule, rule2))
			{
				dependency2.children.get(DependencyMode.WEAK).add(rule);
				dependency.parents.get(DependencyMode.WEAK).add(rule2);
			}
			if(dependsStrongOn(rule, rule2))
			{
				dependency2.children.get(DependencyMode.STRONG).add(rule);
				dependency.parents.get(DependencyMode.STRONG).add(rule2);
			}

			// Vérification des dépendances de rule à rule
			if(dependsWeakOn(rule, rule))
			{
				dependency.children.get(DependencyMode.WEAK).add(rule);
				dependency.parents.get(DependencyMode.WEAK).add(rule);
			}
			if(dependsStrongOn(rule, rule))
			{
				dependency.children.get(DependencyMode.STRONG).add(rule);
				dependency.parents.get(DependencyMode.STRONG).add(rule);
			}
		}
	}

	@Override
	public List<PathRule> closure(PathRule rule, DependencyMode mode)
	{
		Deque<PathRule> rule_queue = new ArrayDeque<>();
		List<PathRule> rule_list = new ArrayList<>();

		rule_queue.add(rule);
		rule_list.add(rule);

		// Tant qu'il y a des règles à explorer
		while(!rule_queue.isEmpty())
		{
			PathRule r = rule_queue.poll();
			RuleDep dep = dependencies.get(r);
			List<PathRule> children = dep.children.get(mode);

			// Pour chaque fils de la règle
			for(PathRule child : children)
			{
				// Si il n'a pas encore été parcouru
				if(!rule_list.contains(child))
				{
					// Il est ajouté à la liste des règles parcourues et à la file des règles à
					// parcourir
					rule_queue.add(child);
					rule_list.add(child);
				}
			}
		}
		return rule_list;
	}

	@Override
	public List<PathRule> reverseClosure(PathRule rule, DependencyMode mode)
	{
		Deque<PathRule> rule_queue = new ArrayDeque<>();
		List<PathRule> rule_list = new ArrayList<>();

		rule_queue.add(rule);
		rule_list.add(rule);

		// Tant qu'il y a des règles à explorer
		while(!rule_queue.isEmpty())
		{
			PathRule r = rule_queue.poll();
			RuleDep dep = dependencies.get(r);
			List<PathRule> parents = dep.parents.get(mode);

			// Pour chaque parent de la règle
			for(PathRule parent : parents)
			{
				// Si il n'a pas encore été parcouru
				if(!rule_list.contains(parent))
				{
					// Il est ajouté à la liste des règles parcourues et à la file des règles à
					// parcourir
					rule_queue.add(parent);
					rule_list.add(parent);
				}
			}
		}
		return rule_list;
	}

	@Override
	public List<PathRule> getChildren(PathRule rule, DependencyMode mode)
	{
		return dependencies.get(rule).children.get(mode);
	}

	@Override
	public List<PathRule> getParents(PathRule rule, DependencyMode mode)
	{
		return dependencies.get(rule).parents.get(mode);
	}

	public boolean dependsWeakOn(PathRule rule1, PathRule rule2)
	{
		Path body1 = rule1.getBody();
		Path head2 = rule2.getHead();

		// Si R2 est existentielle
		// Si R1 est valuée
		// Si R2 n'est pas valuée
		if(rule2.isExistential() && rule1.isValued() && !rule2.isValued())
			return false;

		// Si R2 est enracinée
		if(rule2.isRooted())
		{
			// Si R2 est valuée
			if(rule2.isValued())
				// Si h2 est égal à b1
				return head2.getLabels().equals(body1.getLabels());

			// Si R2 non valuée
			else
				// Si on a pas R2 est existentielle et si
				// Si un suffixe de h2 est préfixe de b1
				return !(rule2.isExistential() && body1.isSuffix(head2)) && //
						body1.hasPrefixInSuffix(head2);
		}

		// Si R2 non enracinée
		else
		{
			// Si R2 est valuée
			if(rule2.isValued())
				// Si un préfixe de h2 est suffixe de b1
				return head2.hasPrefixInSuffix(body1);

			// Si R2 non valuée
			else
				// Si h2 est inclu dans b1 OU
				// Si un préfixe de h2 est suffixe de b1 OU
				// Si un suffixe de h2 est préfixe de b1
				return head2.isIncluded(body1) || //
						head2.hasPrefixInSuffix(body1) || //
						body1.hasPrefixInSuffix(head2);
		}

	}

	public boolean dependsStrongOn(PathRule rule1, PathRule rule2)
	{
		Path body1 = rule1.getBody();
		Path head2 = rule2.getHead();

		// Si R2 est existentielle
		// Si R1 est valuée
		// Si R2 n'est pas valuée
		if(rule2.isExistential() && rule1.isValued() && !rule2.isValued())
			return false;

		// Si R1 est enracinée
		if(rule1.isRooted())
		{
			// Si R1 est valuée
			if(rule1.isValued())
				// Si b1 est égal à h2
				return body1.getLabels().equals(head2.getLabels());

			// Si R1 non valuée
			else
				// Si b1 est préfixe de h2
				return body1.isPrefix(head2);
		}

		// Si R1 non enracinée
		else
		{
			// Si R1 est valuée
			if(rule1.isValued())
				// Si b1 est suffixe de h2
				return body1.isSuffix(head2);

			// Si R1 non valuée
			else
				// Si b1 est inclu dans h2
				return body1.isIncluded(head2);
		}
	}

	@Override
	public List<PathRule> getQueryDependencies(Path query)
	{
		List<PathRule> dep = new ArrayList<>();
		String b = StringUtils.join(query.getLabels(), '.');
		PathRule q = PathRule.create(b, "", query.isRooted(), true);

		for(Map.Entry<PathRule, RuleDep> entry : dependencies.entrySet())
		{
			PathRule rule = entry.getKey();
			if(dependsWeakOn(q, rule) || dependsStrongOn(q, rule))
				dep.add(rule);
		}
		return dep;
	}
}
