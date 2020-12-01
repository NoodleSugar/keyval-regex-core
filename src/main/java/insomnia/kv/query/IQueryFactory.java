package insomnia.kv.query;

import java.util.List;

import insomnia.kv.summary.ISummary;

public interface IQueryFactory
{
	List<String> getQueries(List<String> paths, ISummary summary);
}
