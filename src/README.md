# CI Intergration  

This is a gradle plugin we use to automate our podcast validation and release management.

## Expected Files

This plugin expects that the following files are present (location configurable via gradle plugin).

#### iTunes.yml file
Contains all the general information about the podcast.
Must look as follows:

```yml
title: "The Context  #androiddev"
link: https://github.com/artem-zinnatullin/TheContext-Podcast
description: New Podcast about Android Development
language: en-us
rssLink: https://raw.githubusercontent.com/artem-zinnatullin/TheContext-Podcast/master/feed.rss
keywords: android,androiddev,context,development,software
subtitle: New Android Developers Podcast
summary: The Context is a podcast about Android Development
imageUrl: https://raw.githubusercontent.com/artem-zinnatullin/TheContext-Podcast/master/logo.png
author: Artem Zinnatullin
categories:
    - Software How-To
    - Android
```


#### People File
A file containing people. They can be linked from within show notes file with `@PersonId@`.
File must look as follows:

```yml
- id: ArtemZinnatullin # can be referenced later as @ArtemZinnatulin@
  name: Artem Zinnatulin
  twitter: artem_zin
  github: artem-zinnatullin
  website: https://artemzin.com
- id: HannesDorfmann # can be referenced later as @HannesDorfmann@
  name: Hannes Dorfmann
  twitter: sockeqwe
  github: sockeqwe
  website: http://hannesdorfmann.com
```

#### Episodes directory

A directory containing all episodes. Each Episode must be a single .yml file like this:
```yml
  number: 10
  title: Kotlin Language Design Nitpicking with Dmitry Jemerov from JetBrains
  hosts:
    < hannes.dorfmann
    < artem.zinnatullin
  guests:
    < dmitry.jemerov
  links:
    - title: Book: Kotlin in Action written by Dmitry Jemerov and Svetlana Isakova
      url: https://www.manning.com/books/kotlin-in-action
    - title: Official Kotlin website with photo of lighthouse on Kotlin island
      url: http://kotlinlang.org/
  
person:
  id: hannes.dorfmann
  name: Hannes Dorfmann
  twitter: sockeqwe (Optional)
  blog: http://hannesdorfmann.com/ (Optional)
  github: sockeqwe (Optional)
```


## Usage

In `build.gradle` file add:

```gradle
buildscript {
    dependencies {
        classpath "io.thecontext.ci:podcast:0.1.0"
    }
}

apply plugin: 'io.thecontext.ci.podcast'


// optional config
podcast {
    episodesDir "path/to/director/with/episodefiles"
    itunesConfig "path/to/itunes.yml"
}
```


## Validation task

This task should run on each pull request.
It checks if :
 - the markdown of the show notes is correct
 - all url's are linking to a valid destinations (checks if returned http status code is a 2xx)
 - validates the resulting RSS feed after the changes of the pull request have been applied.

Otherwise the build fails. 

If everything changed by the pull request is valid, a full preview of the show notes will be posted back to the original pull request for easier reviewing

Run this task with
```gradle
./gradlew validation
```