package com.wangjiegulu.sifcache_lib.ext

@NoArg
public data class SifPair<out A, out B>(
    public val first: A,
    public val second: B
)

@NoArg
public data class SifTriple<out A, out B, out C>(
    public val first: A,
    public val second: B,
    public val third: C
)