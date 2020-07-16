package insomnia.rule.ontology;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import insomnia.rule.PathRule;

public class PathGRD implements IGRD<PathRule>
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
			if(rule2.dependsWeakOn(rule))
			{
				dependency.children.get(DependencyMode.WEAK).add(rule2);
				dependency2.parents.get(DependencyMode.WEAK).add(rule);
			}
			if(rule2.dependsStrongOn(rule))
			{
				dependency.children.get(DependencyMode.STRONG).add(rule2);
				dependency2.parents.get(DependencyMode.STRONG).add(rule);
			}

			// Vérification des dépendances de rule à rule2
			if(rule.dependsWeakOn(rule2))
			{
				dependency2.children.get(DependencyMode.WEAK).add(rule);
				dependency.parents.get(DependencyMode.WEAK).add(rule2);
			}
			if(rule.dependsStrongOn(rule2))
			{
				dependency2.children.get(DependencyMode.STRONG).add(rule);
				dependency.parents.get(DependencyMode.STRONG).add(rule2);
			}

			// Vérification des dépendances de rule à rule
			if(rule.dependsWeakOn(rule))
			{
				dependency.children.get(DependencyMode.WEAK).add(rule);
				dependency.parents.get(DependencyMode.WEAK).add(rule);
			}
			if(rule.dependsStrongOn(rule))
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
}
