package suwayomi.tachidesk.manga

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import io.javalin.apibuilder.ApiBuilder.delete
import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.apibuilder.ApiBuilder.patch
import io.javalin.apibuilder.ApiBuilder.path
import io.javalin.apibuilder.ApiBuilder.post
import io.javalin.apibuilder.ApiBuilder.ws
import suwayomi.tachidesk.manga.controller.BackupController
import suwayomi.tachidesk.manga.controller.CategoryController
import suwayomi.tachidesk.manga.controller.DownloadController
import suwayomi.tachidesk.manga.controller.ExtensionController
import suwayomi.tachidesk.manga.controller.MangaController
import suwayomi.tachidesk.manga.controller.SourceController

object MangaAPI {
    fun defineEndpoints() {
        path("extension") {
            get("list", ExtensionController::list)

            get("install/:pkgName", ExtensionController::install)
            post("install", ExtensionController::installFile)
            get("update/:pkgName", ExtensionController::update)
            get("uninstall/:pkgName", ExtensionController::uninstall)

            get("icon/:apkName", ExtensionController::icon)
        }

        path("source") {
            get("list", SourceController::list)
            get(":sourceId", SourceController::retrieve)

            get(":sourceId/popular/:pageNum", SourceController::popular)
            get(":sourceId/latest/:pageNum", SourceController::latest)

            get(":sourceId/preferences", SourceController::getPreferences)
            post(":sourceId/preferences", SourceController::setPreference)

            get(":sourceId/filters", SourceController::filters)

            get(":sourceId/search/:searchTerm/:pageNum", SourceController::searchSingle)
//            get("search/:searchTerm/:pageNum", SourceController::searchGlobal)
        }

        path("manga") {
            get(":mangaId", MangaController::retrieve)
            get(":mangaId/thumbnail", MangaController::thumbnail)

            get(":mangaId/category", MangaController::categoryList)
            get(":mangaId/category/:categoryId", MangaController::addToCategory)
            delete(":mangaId/category/:categoryId", MangaController::removeFromCategory)

            get(":mangaId/library", MangaController::addToLibrary)
            delete(":mangaId/library", MangaController::removeFromLibrary)

            patch(":mangaId/meta", MangaController::meta)

            get(":mangaId/chapters", MangaController::chapterList)
            get(":mangaId/chapter/:chapterIndex", MangaController::chapterRetrieve)
            patch(":mangaId/chapter/:chapterIndex", MangaController::chapterModify)

            patch(":mangaId/chapter/:chapterIndex/meta", MangaController::chapterMeta)

            get(":mangaId/chapter/:chapterIndex/page/:index", MangaController::pageRetrieve)
        }

        path("category") {
            get("", CategoryController::categoryList)
            post("", CategoryController::categoryCreate)

            get(":categoryId", CategoryController::categoryMangas)
            patch(":categoryId", CategoryController::categoryModify)
            delete(":categoryId", CategoryController::categoryDelete)

            patch("reorder", CategoryController::categoryReorder)
        }

        path("backup") {
            post("import", BackupController::protobufImport)
            post("import/file", BackupController::protobufImportFile)

            post("validate", BackupController::protobufValidate)
            post("validate/file", BackupController::protobufValidateFile)

            get("export", BackupController::protobufExport)
            get("export/file", BackupController::protobufExportFile)
        }

        path("downloads") {
            ws("", DownloadController::downloadsWS)

            get("start", DownloadController::start)
            get("stop", DownloadController::stop)
            get("clear", DownloadController::stop)
        }

        path("download") {
            get(":mangaId/chapter/:chapterIndex", DownloadController::queueChapter)
            delete(":mangaId/chapter/:chapterIndex", DownloadController::unqueueChapter)
        }
    }
}
