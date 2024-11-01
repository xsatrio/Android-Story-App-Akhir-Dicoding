package com.dicoding.storyapp

import com.dicoding.storyapp.data.local.database.StoryEntity
import java.util.UUID
import kotlin.random.Random

object DataDummy {

    fun generateDummyStoryResponse(): List<StoryEntity> {
        val items: MutableList<StoryEntity> = arrayListOf()
        for (i in 0..100) {
            val story = StoryEntity(
                photoUrl = "https://picsum.photos/200/300?random=$i",
                createdAt = "2024-10-24T12:00:00Z",
                name = "Author $i",
                description = "This is a description for story $i",
                lon = Random.nextDouble(-180.0, 180.0),
                id = UUID.randomUUID().toString(),
                lat = Random.nextDouble(-90.0, 90.0)
            )
            items.add(story)
        }
        return items
    }
}
