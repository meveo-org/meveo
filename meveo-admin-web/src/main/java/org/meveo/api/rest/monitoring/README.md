# Monitoring resource

Go to `/meveo/api/rest/monitoring` to monitor all the active pools, caches, etc ...

```json
{
    "pools": {
        "endpoints": {
            "pooled-endpoint": {
                "nbActive": 0,
                "nbIdle": 2,
                "nbTotal": 2
            }
        }
    }
}
```

If you want to get detailed information, for instance of the pool for the `pooled-endpoint` endpoint, you can go to `/meveo/api/rest/monitoring/pools/endpoints/pooled-endpoint`

```json
{
    "nbActive": 0,
    "nbIdle": 2,
    "nbTotal": 2
}
```

You can even retrieve only the number of idling instance in the above pool by going to `/meveo/api/rest/monitoring/pools/endpoints/pooled-endpoint/nbIdle`

```json
2
```
