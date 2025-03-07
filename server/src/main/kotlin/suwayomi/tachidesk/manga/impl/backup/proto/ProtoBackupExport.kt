package suwayomi.tachidesk.manga.impl.backup.proto

/*
 * Copyright (C) Contributors to the Suwayomi project
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import kotlinx.coroutines.runBlocking
import okio.buffer
import okio.gzip
import okio.sink
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import suwayomi.tachidesk.manga.impl.CategoryManga
import suwayomi.tachidesk.manga.impl.Chapter
import suwayomi.tachidesk.manga.impl.backup.BackupFlags
import suwayomi.tachidesk.manga.impl.backup.proto.models.Backup
import suwayomi.tachidesk.manga.impl.backup.proto.models.BackupCategory
import suwayomi.tachidesk.manga.impl.backup.proto.models.BackupChapter
import suwayomi.tachidesk.manga.impl.backup.proto.models.BackupManga
import suwayomi.tachidesk.manga.impl.backup.proto.models.BackupSerializer
import suwayomi.tachidesk.manga.impl.backup.proto.models.BackupSource
import suwayomi.tachidesk.manga.model.table.CategoryTable
import suwayomi.tachidesk.manga.model.table.MangaStatus
import suwayomi.tachidesk.manga.model.table.MangaTable
import suwayomi.tachidesk.manga.model.table.SourceTable
import suwayomi.tachidesk.manga.model.table.toDataClass
import java.io.ByteArrayOutputStream
import java.io.InputStream

object ProtoBackupExport : ProtoBackupBase() {
    suspend fun createBackup(flags: BackupFlags): InputStream {
        // Create root object

        val databaseManga = transaction { MangaTable.select { MangaTable.inLibrary eq true } }

        val backup: Backup = transaction {
            Backup(
                backupManga(databaseManga, flags),
                backupCategories(),
                emptyList(),
                backupExtensionInfo(databaseManga)
            )
        }

        val byteArray = parser.encodeToByteArray(BackupSerializer, backup)

        val byteStream = ByteArrayOutputStream()
        byteStream.sink().gzip().buffer().use { it.write(byteArray) }

        return byteStream.toByteArray().inputStream()
    }

    private fun backupManga(databaseManga: Query, flags: BackupFlags): List<BackupManga> {
        return databaseManga.map { mangaRow ->
            val backupManga = BackupManga(
                mangaRow[MangaTable.sourceReference],
                mangaRow[MangaTable.url],
                mangaRow[MangaTable.title],
                mangaRow[MangaTable.artist],
                mangaRow[MangaTable.author],
                mangaRow[MangaTable.description],
                mangaRow[MangaTable.genre]?.split(", ") ?: emptyList(),
                MangaStatus.valueOf(mangaRow[MangaTable.status]).value,
                mangaRow[MangaTable.thumbnail_url],
                0, // not supported in Tachidesk
                0, // not supported in Tachidesk
            )

            val mangaId = mangaRow[MangaTable.id].value

            if (flags.includeChapters) {
                val chapters = runBlocking { Chapter.getChapterList(mangaId) }
                backupManga.chapters = chapters.map {
                    BackupChapter(
                        it.url,
                        it.name,
                        it.scanlator,
                        it.read,
                        it.bookmarked,
                        it.lastPageRead,
                        0, // not supported in Tachidesk
                        it.uploadDate,
                        it.chapterNumber,
                        it.index,
                    )
                }
            }

            if (flags.includeCategories) {
                backupManga.categories = CategoryManga.getMangaCategories(mangaId).map { it.order }
            }

//            if(flags.includeTracking) {
//                backupManga.tracking = TODO()
//            }

//            if (flags.includeHistory) {
//                backupManga.history = TODO()
//            }

            backupManga
        }
    }

    private fun backupCategories(): List<BackupCategory> {
        return CategoryTable.selectAll().orderBy(CategoryTable.order to SortOrder.ASC).map {
            CategoryTable.toDataClass(it)
        }.map {
            BackupCategory(
                it.name,
                it.order,
                0, // not supported in Tachidesk
            )
        }
    }

    private fun backupExtensionInfo(mangas: Query): List<BackupSource> {
        return mangas
            .asSequence()
            .map { it[MangaTable.sourceReference] }
            .distinct()
            .map {
                val sourceRow = SourceTable.select { SourceTable.id eq it }.firstOrNull()
                BackupSource(
                    sourceRow?.get(SourceTable.name) ?: "",
                    it
                )
            }
            .toList()
    }
}
