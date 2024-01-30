pip install databricks-sql-connector
import sql
import pandas as pd

server_hostname=
http_path      =
access_token   =

connection     = sql.connect(
                            server_hostname = server_hostname
                            http_path       = http_path
                            access_token    = access_token
                            )    

Query Before = """

                """
