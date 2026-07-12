package me.rerere.search

object TavilySearchService : SearchService<SearchServiceOptions.TavilyOptions> {
    override val name: String = "Tavily"

    override suspend fun search(
        query: String,
        commonOptions: SearchCommonOptions,
        serviceOptions: SearchServiceOptions.TavilyOptions
    ): SearchResult {
        TODO("Not yet implemented")
    }
}