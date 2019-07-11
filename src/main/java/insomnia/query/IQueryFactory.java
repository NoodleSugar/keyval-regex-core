package insomnia.query;

import java.util.List;

import insomnia.summary.ISummary;

public interface IQueryFactory
{
	List<String> getQueries(List<String> paths, ISummary summary);
}
