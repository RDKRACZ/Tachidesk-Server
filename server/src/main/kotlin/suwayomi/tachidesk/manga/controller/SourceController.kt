package suwayomi.tachidesk.manga.controller

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import io.javalin.http.Context
import suwayomi.tachidesk.manga.impl.MangaList
import suwayomi.tachidesk.manga.impl.Search
import suwayomi.tachidesk.manga.impl.Source
import suwayomi.tachidesk.manga.impl.Source.SourcePreferenceChange
import suwayomi.tachidesk.server.JavalinSetup.future

object SourceController {
    /** list of sources */
    fun list(ctx: Context) {
        ctx.json(Source.getSourceList())
    }

    /** fetch source with id `sourceId` */
    fun retrieve(ctx: Context) {
        val sourceId = ctx.pathParam("sourceId").toLong()
        ctx.json(Source.getSource(sourceId))
    }

    /** popular mangas from source with id `sourceId` */
    fun popular(ctx: Context) {
        val sourceId = ctx.pathParam("sourceId").toLong()
        val pageNum = ctx.pathParam("pageNum").toInt()
        ctx.json(
            future {
                MangaList.getMangaList(sourceId, pageNum, popular = true)
            }
        )
    }

    /** latest mangas from source with id `sourceId` */
    fun latest(ctx: Context) {
        val sourceId = ctx.pathParam("sourceId").toLong()
        val pageNum = ctx.pathParam("pageNum").toInt()
        ctx.json(
            future {
                MangaList.getMangaList(sourceId, pageNum, popular = false)
            }
        )
    }

    /** fetch preferences of source with id `sourceId` */
    fun getPreferences(ctx: Context) {
        val sourceId = ctx.pathParam("sourceId").toLong()
        ctx.json(Source.getSourcePreferences(sourceId))
    }

    /** fetch preferences of source with id `sourceId` */
    fun setPreference(ctx: Context) {
        val sourceId = ctx.pathParam("sourceId").toLong()
        val preferenceChange = ctx.bodyAsClass(SourcePreferenceChange::class.java)
        ctx.json(Source.setSourcePreference(sourceId, preferenceChange))
    }

    /** fetch filters of source with id `sourceId` */
    fun filters(ctx: Context) {
        val sourceId = ctx.pathParam("sourceId").toLong()
        val reset = ctx.queryParam("reset", "false").toBoolean()

        ctx.json(Search.getInitialFilterList(sourceId, reset))
    }

    /** single source search */
    fun searchSingle(ctx: Context) {
        val sourceId = ctx.pathParam("sourceId").toLong()
        val searchTerm = ctx.pathParam("searchTerm")
        val pageNum = ctx.pathParam("pageNum").toInt()
        ctx.json(future { Search.sourceSearch(sourceId, searchTerm, pageNum) })
    }

    /** all source search */
    fun searchAll(ctx: Context) { // TODO
        val searchTerm = ctx.pathParam("searchTerm")
        ctx.json(Search.sourceGlobalSearch(searchTerm))
    }
}
