package com.studentcompanion.app.data.remote.rest

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The REST backend has no Firestore-style push, so every Rest*RepositoryImpl
 * read method is naturally a single-shot `flow { emit(...) }`. Each write
 * method calls [notifyChanged] after a successful mutation, and every read
 * method re-runs its fetch on [changes] (in addition to its own initial
 * emission) — a shared, app-wide signal rather than six separate ad hoc
 * per-repository caches, since one mutation (e.g. completing a task awards
 * points too) can affect more than one repository's data.
 *
 * This only covers changes made from THIS app instance in THIS process — it
 * is not a substitute for live multi-device sync, which would need real
 * push (websockets/polling) and is out of scope for this migration step.
 */
@Singleton
class RestDataRefreshBus @Inject constructor() {
    private val _changes = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val changes: SharedFlow<Unit> = _changes

    fun notifyChanged() {
        _changes.tryEmit(Unit)
    }
}
