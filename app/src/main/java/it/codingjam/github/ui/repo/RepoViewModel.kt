/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.codingjam.github.ui.repo

import android.arch.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import it.codingjam.github.NavigationController
import it.codingjam.github.repository.RepoRepository
import it.codingjam.github.util.LiveDataDelegate
import it.codingjam.github.util.UiActionsLiveData
import it.codingjam.github.vo.RepoId
import it.codingjam.github.vo.Resource
import javax.inject.Inject

class RepoViewModel @Inject constructor(
        private val navigationController: NavigationController,
        private val repository: RepoRepository
) : ViewModel() {

    private val disposable = CompositeDisposable()

    private lateinit var repoId: RepoId

    val liveData = LiveDataDelegate(RepoViewState(Resource.Empty))

    private var state by liveData

    val uiActions = UiActionsLiveData()

    fun retry() = reload()

    fun init(repoId: RepoId) {
        this.repoId = repoId
        reload()
    }

    fun reload() {
        state = state.copy(Resource.Loading)

        disposable += repository.loadRepo(repoId.owner, repoId.name)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { state = state.copy(Resource.Success(it)) },
                        { state = state.copy(Resource.Error(it)) }
                )
    }

    fun openUserDetail(login: String) =
        uiActions { navigationController.navigateToUser(it, login) }

    override fun onCleared() = disposable.clear()
}
