package rule.tree;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import insomnia.rule.tree.Tree;
import insomnia.rule.tree.TreeBuilder;
import insomnia.rule.tree.node.TreeNode;

class TestTree
{

	@Test
	void test()
	{
		TreeBuilder builder = new TreeBuilder(false);
		builder.addChild("a").addChild("b");
		builder.goToChild(0).addChild("c").backToParent();
		builder.goToChild(1).addChild("d");
		Tree t = builder.build();
		TreeNode root = t.getRoot();
		
		assertFalse(t.isRooted());
		assertTrue(root != null);
	}

}
