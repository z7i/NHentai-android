package moe.feng.nhentai.util.extension

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

fun <T> Observable<T>.io2mainThread(): Observable<T> =
		this.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

fun <T> Observable<T>.computation2mainThread(): Observable<T> =
		this.subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())

fun <T> Flowable<T>.io2mainThread(): Flowable<T> =
		this.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

fun <T> Flowable<T>.computation2mainThread(): Flowable<T> =
		this.subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())

fun <T> Single<T>.io2mainThread(): Single<T> =
		this.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

fun <T> Single<T>.computation2mainThread(): Single<T> =
		this.subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())