package suwayomi.tachidesk.server.database.migration

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import de.neonew.exposed.migrations.helpers.SQLMigration

@Suppress("ClassName", "unused")
class M0015_SourceAndExtensionLangAddLengthLimit : SQLMigration() {
    override val sql =
        """
        ALTER TABLE SOURCE ALTER COLUMN LANG VARCHAR(32);
        ALTER TABLE EXTENSION ALTER COLUMN LANG VARCHAR(32);
        """.trimIndent()
}
