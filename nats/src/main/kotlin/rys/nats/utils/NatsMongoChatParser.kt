package rys.nats.utils

import org.bson.types.ObjectId
import rys.nats.protostest.Mongochat

import rys.rest.model.MongoChat

object NatsMongoChatParser {
    fun parse(message: String): MongoChat? {
        val id = extractId(message)
        val name = extractName(message) ?: return null
        val users = extractUsers(message) ?: return null
        return MongoChat(id = id, name = name, users = users)
    }

    private fun extractId(message: String) : ObjectId? {
        val idRegex = "MongoChat\\(id=([^,]+),".toRegex()
        val idMatch = idRegex.find(message) ?: return null
        val idString = if (idMatch.groupValues[1]!="null") idMatch.groupValues[1] else return null
        return ObjectId(idString)
    }

    private fun extractName(message: String): String? {
        val idRegex = "MongoChat\\(id=([^,]+),".toRegex()
        val idMatch = idRegex.find(message) ?: return null

        val usersStartIndex = message.indexOf("users=[")
        val nameStartIndex = idMatch.range.last + 7
        val nameEndIndex = usersStartIndex - 2

        return if (nameStartIndex < nameEndIndex && nameStartIndex >= 0) {
            message.substring(nameStartIndex, nameEndIndex)
        } else {
            null
        }
    }

    private fun extractUsers(message: String): List<ObjectId>? {
        val usersStartIndex = message.indexOf("users=[")
        if (usersStartIndex == -1) return null

        val afterIdString = message.substring(usersStartIndex + 7)
        val usersEndIndex = afterIdString.lastIndexOf("]")
        if (usersEndIndex == -1) return null

        val usersString = afterIdString.substring(0, usersEndIndex)
        val users = usersString.split(", ").mapNotNull { it.trim().takeIf { it.isNotEmpty() }?.let { ObjectId(it) } }

        return if (users.isNotEmpty()) users else null
    }

}
