# Podcaster

A tool for generating podcast RSS feed and Hugo-based website
from YAML and Markdown files.

## Sample

Please refer to the integration test inputs and outputs located
at `src/test/resources/integration/`.

## Usage

```
Usage: podcaster [options]
  Options:
  * --input
      Path to a directory with YAML and Markdown declarations.
  * --output-feed
      Path to a file which will contain resulting RSS feed.
  * --output-website
      Path to a directory which will contain resulting Markdown files.
```

### Input

```
.
├── 01-dogs
│   ├── episode.yaml
│   └── notes.md
├── 02-cats
│   ├── episode.yaml
│   └── notes.md
├── 03-plants
│   ├── episode.yaml
│   └── notes.md
├── people.yaml
└── podcast.yaml
```

#### `people.yaml`

Name         | Required | Description
-------------|----------|------------
`id`         | Yes      | ID used to reference the person.
`name`       | Yes      |
`email`      | Kind of  | Contact email, currently used for podcast owner only, per Apple and Google requirements.
`twitter`    | No       | Twitter username.
`github`     | No       | GitHub username.
`links`      | No       |
`links.name` | Yes      |
`links.url`  | Yes      |

#### `podcast.yaml`

Name              | Required | Description
------------------|----------|------------
`title`           | Yes      |
`description`     | Yes      |
`url`             | Yes      | URL for the web presense.
`artworkUrl`      | Yes      |
`migrationFeedUrl`| No       | URL pointing to a new feed location.
`language`        | Yes      | ISO 639 code.
`explicit`        | Yes      | `true` or `false`.
`category`        | Yes      | iTunes one.
`subcategory`     | Yes      | iTunes one.
`people.owner`    | Yes      | ID from `people.yaml`.
`people.authors`  | Yes      | IDs from `people.yaml`.

#### `{episode}/episode.yaml`

Name             | Required | Description
-----------------|----------|-----------
`id`             | Yes      | Unique ID for the episode, needs to be unique among all episodes.
`number`         | Yes      | Number.
`part`           | No       | Number.
`title`          | Yes      | Do not put neither number or part here, both will be added automatically.
`description`    | Yes      |
`time`           | Yes      | ISO 8601 date-time.
`duration`       | Yes      | ISO 8601 time.
`file.url`       | Yes      |
`file.length`    | Yes      | Number of bytes.
`people.hosts`   | Yes      | IDs from `people.yaml`.
`people.guests`  | No       | IDs from `people.yaml`.
`discussionUrl`  | Yes      |
`slug`           | Implicit | The name of the episode directory is used as a `slug`, no need to specify it in YAML.

### Output

```
.
├── feed.rss
└── website
  ├── 01-dogs.md
  ├── 02-cats.md
  └── 03-plants.md
```

The content depends on the used template which are hardcoded at this point.
Please contact us if you are interested in using a different one.

## Feed Specs

Feed is being rendered based on following specs:

* [Apple](https://help.apple.com/itc/podcasts_connect/#/itcb54353390)
* [Google for developers](https://developers.google.com/actions/content-actions/podcasts) and
  [Google for podcasters](https://support.google.com/googleplay/podcasts/answer/6260341)
* [Spotify](https://podcasters.spotify.com/terms/Spotify_Podcast_Delivery_Specification_v1.5.pdf)
