# Faster That Bike
A data tool for aggregating bike data to make recommendations and show comparisons. The goal is to use metrics to provide users with a calculated "ride feel" score, though of course you should test out a bike at your local bike shop before making any big decisions.

**Work in progress.** Currently only supports Trek, Giant, Specialized, and Cannondale, more brands plus recommendation features to come.

## What it does
By aggregating data across bike brands, users will be able to use the site to directly compare geometries and ride feel predictions between any number of bikes.

## Data Pipeline
1. **Scrape** - each brand has a designated scraper to pull a list of current models and their geometries. For sites that use images of geometry tables, a Claude Vision API is used to parse the data.
2. **Compute indexes** - using geometry metrics, ride feel characters are predicted and z-scored within size buckets, then normalized using min/max scaling to generate a score 0-100.
3. **Persist** - bike list, geometry metrics, ride feel scores are upserted into PostgresSQL database
4. **Serve** - a Spring Boot REST API feeds a React frontend with scatter plot,
   filter, and bike list views

## Tech Stack
**Backend:** Java 21, Spring Boot 3, PostgreSQL, JDBC
**Frontend:** React (Vite), Recharts
**Scraping:** Jsoup, Jackson, Anthropic Java SDK (vision extraction for geometry table images)