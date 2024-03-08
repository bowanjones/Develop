# Facility Compliance Files (Company)

from pyspark.sql import SparkSession

 

#Operational

from datetime import datetime #to parameterize the date time

from pyspark.sql import functions as sf

from pyspark.sql.functions import col, unix_timestamp, to_date

import time

from pyspark.sql.types import *

import re

import logging

import pandas as pd

import io

import numpy as np

 

#ECS Import

import boto3

from botocore.client import Config

 

#For Error handling

import sys

 

#for passed in parameters

import argparse

 

# WHEEL FILE DEPENDENCY

# Custom shared packages

# from gm_pyspark_etl import validate

# from gm_pyspark_etl import functions as gm_sf

# from gm_pyspark_etl import s3 as gm_s3

# from gm_pyspark_etl import hive as gm_hive

 

print('Pandas Version: ' + pd.__version__)

 

# instantiate SparkSession

 

spark = (

            SparkSession.builder

            .enableHiveSupport()

            .getOrCreate()

)

 

#boto3 connection

 

session = boto3.session.Session()

 

s3_client = session.client(

            service_name = 's3',

            aws_access_key_id = spark.conf.get("spark.hadoop.fs.s3a.access.key"),

            aws_secret_access_key = spark.conf.get("spark.hadoop.fs.s3a.secret.key"),

            endpoint_url = spark.conf.get("spark.hadoop.fs.s3a.endpoint"),

            config=Config(s3={'addressing_style': 'path'})

)

#############################################################################

# READ COMMAND LINE ARGS

#############################################################################

ENV=sys.argv[1]

LOGGING_FILE=sys.argv[2]

 

#############################################################################

# DEFINE STATIC CONFIG VARIABLES

#############################################################################

STAGING_TABLE_NAME = "dl_edge_base_avista_171749_base_dcr_dcr.company_fac_img_cmplnc_day"

STAGING_TABLE_PATH = f"/sync/{ENV.lower()}_42124_edw_dl_b/DL/EDGE_BASE/**

 

 

# complex logic for choosing most recent bac dependent on compliance dates

def excel_to_sdf(bucket,searchPattern,skiprows):

            resp = s3_client.list_objects_v2(Bucket=bucket)

            try:

                        contents = resp['Contents']

            except KeyError:

                        print(f"No contents in the {bucket}")

                        return None

            else:

                        sdf=None

                        for file_object in resp['Contents']:

                                    filename=file_object.get('Key')

                                   

                                    # Search Pattern

                                    if searchPattern in filename:

                                               

                                                file_dt = '20' + filename[-6:-4] + '-' + filename[-10:-8] + '-' + filename[-8:-6]

                                                obj = s3_client.get_object(Bucket=bucket, Key=filename)

                                               

                                                pdf_excel = pd.read_csv(io.BytesIO(obj['Body'].read()),keep_default_na=False,skiprows=skiprows)

                                                pdf_excel = pdf_excel.applymap(str)

                                               

                                                pdf_excel.columns= ['biz_assoc_id','sched_qtr_nbr','wave_yr_id','gm_cmplnt_dt','cmplnc_revw_dt']

                                                temp = ''

                                                pdf_excel.insert(loc = 3, column = 'src_sys_id', value = temp)

                                                pdf_excel.insert(loc = 4, column = 'src_file_ind', value = temp)

                                               

                                                # creates list of indexs that are duplicates

                                                dup_idx = list(pdf_excel[pdf_excel.biz_assoc_id.duplicated(keep=False)].index)

                                                dup_val = []

                                               

                                                # creates list of values that are duplicates

                                                for x in dup_idx:

                                                            dup_val.append(pdf_excel['biz_assoc_id'].iloc[x])

                                               

                                                dup_val = list(dict.fromkeys(dup_val))

                                               

                                                pdf_excel['gm_cmplnt_dt'] = pdf_excel['gm_cmplnt_dt'].replace(r'', '01/01/1000')

                                                pdf_excel['cmplnc_revw_dt'] = pdf_excel['cmplnc_revw_dt'].replace(r'', '1000-01-01')

                                                drop_me = []

                                               

                                                # iterate over duplicated values

                                                for val in dup_val:

                                                            cmplnt_dt = []

                                                            cmplnt_dt_idx = []

                                                            cmplnc_revw_dt = []

                                                            cmplnc_revw_dt_idx = []

                                                           

                                                            temp = list(pdf_excel.loc[pdf_excel['biz_assoc_id'] == val].index)

                                                            # grabbing indexes

                                                            for idx in temp:

                                                                        test = []

                                                            cmplnt_dt.append(datetime.strptime(pdf_excel.iloc[idx]['gm_cmplnt_dt'], '%m/%d/%Y').date())

                                                                        cmplnt_dt_idx.append(idx)

                                                            cmplnc_revw_dt.append(datetime.strptime(pdf_excel.iloc[idx]['cmplnc_revw_dt'],  '%Y-%m-%d').date())

                                                                        cmplnc_revw_dt_idx.append(idx)

                                                           

                                                           

                                                            recent_cmplnt_dt = max(cmplnt_dt)

                                                            recent_cmplnc_revw_dt = max(cmplnc_revw_dt)

                                                           

                                                           

                                                            cmplnt_mx_idx = [i for i,val in enumerate(cmplnt_dt) if val==recent_cmplnt_dt]

                                                            revw_cmplnt_mx_idx = [i for i,val in enumerate(cmplnc_revw_dt) if val==recent_cmplnc_revw_dt]

                                                           

                                                            str_cmpln_test = str(cmplnt_mx_idx).replace('[', '').replace(']', '')

                                                            str_revw_test = str(revw_cmplnt_mx_idx).replace('[', '').replace(']', '')

                                                           

                                                            # conditional length greater than two for comparision to start

                                                            if len(cmplnt_mx_idx) >= 2:

                                                           

                                                                        # deciding which bac to keep

                                                                        if str_revw_test in str_cmpln_test and len(revw_cmplnt_mx_idx) < 2:

                                                                                    keep_me = str(cmplnt_dt[revw_cmplnt_mx_idx[0]]) + ', ' + str(cmplnc_revw_dt[revw_cmplnt_mx_idx[0]])

                                                                                   

                                                                                    remove_me = [y for y,y in enumerate(cmplnt_dt_idx) if y != cmplnt_dt_idx[revw_cmplnt_mx_idx[0]]]

                                                                                   

                                                                                   

                                                                                   

                                                                        elif str_cmpln_test in str_revw_test and len(revw_cmplnt_mx_idx) == len(cmplnt_mx_idx):

                                                                                    keep_me = str(cmplnt_dt[cmplnt_mx_idx[0]]) + ', ' + str(cmplnc_revw_dt[cmplnt_mx_idx[0]])

                                                                                    remove_me = [y for y,y in enumerate(cmplnc_revw_dt_idx) if y != cmplnt_dt_idx[cmplnt_mx_idx[0]]]

                                                                                    drop_me.extend(remove_me)

                                                                        else:

                                                                                    df_cmplnt_idx = cmplnt_dt_idx[cmplnt_dt.index(recent_cmplnt_dt)]

                                                                                    my_range = range(len(cmplnt_dt_idx))

                                                                                    for i in range(len(cmplnt_dt_idx)):

                                                                                                j = cmplnt_dt_idx[i]

                                                                                                temp = datetime.strptime(pdf_excel.iloc[j]['cmplnc_revw_dt'],  '%Y-%m-%d').date()

                                                                                                if i+1 < len(cmplnt_dt_idx):

                                                                                                            if temp > datetime.strptime(pdf_excel.iloc[cmplnt_dt_idx[i+1]]['cmplnc_revw_dt'],  '%Y-%m-%d').date():

                                                                                                                        temp = datetime.strptime(pdf_excel.iloc[j]['cmplnc_revw_dt'],  '%Y-%m-%d').date()

                                                                                                            elif temp < datetime.strptime(pdf_excel.iloc[cmplnt_dt_idx[i+1]]['cmplnc_revw_dt'],  '%Y-%m-%d').date():

                                                                                                                        temp = datetime.strptime(pdf_excel.iloc[cmplnt_dt_idx[i+1]]['cmplnc_revw_dt'],  '%Y-%m-%d').date()

                                                                                                else:

                                                                                                            pass

                                                                                   

                                                                                    keep_me = temp

                                                                                    remove_me = [x for x, x in enumerate(cmplnt_dt_idx) if x!=cmplnt_dt_idx[i]]

                                                                                    drop_me.extend(remove_me)

                                                           

                                                            # two bac were not compared

                                                            else:

                                                                        keep_me = str(cmplnt_dt[cmplnt_mx_idx[0]]) + ', ' + str(cmplnc_revw_dt[cmplnt_mx_idx[0]])

                                                                        remove_me = [y for y,y in enumerate(cmplnc_revw_dt_idx) if y != cmplnt_dt_idx[cmplnt_mx_idx[0]]]

                                                                        drop_me.extend(remove_me)

                                                                       

                                               

                                                # drop the ones from this list

                                                pdf_excel.drop(drop_me, inplace=True)

                                               

                                                # by-passing 

                                                pdf_excel['gm_cmplnt_dt'] = pdf_excel['gm_cmplnt_dt'].replace('01/01/1000', 20010101)

                                                pdf_excel['cmplnc_revw_dt'] = pdf_excel['cmplnc_revw_dt'].replace('1000-01-01', 20010101)

                                               

                                                pdf_excel['gm_cmplnt_dt'] = pdf_excel['gm_cmplnt_dt'].astype(str)

                                                pdf_excel['gm_cmplnt_dt'] = pd.to_datetime(pdf_excel['gm_cmplnt_dt'])

                                                pdf_excel['gm_cmplnt_dt'] = pdf_excel['gm_cmplnt_dt'].dt.strftime('%Y/%m/%d')

                                               

                                                # filename parse conditional for 2022 compliance vs. 2017 compliance

                                                if '2022' in filename:

                                                            # static

                                                            pdf_excel['src_file_ind'] = '2022'

                                                else:

                                                            pdf_excel['src_file_ind'] = '2017'

                                               

                                                # reordering columns

                                                temp = pdf_excel['cmplnc_revw_dt']

                                                pdf_excel = pdf_excel.drop('cmplnc_revw_dt', axis=1)

                                                pdf_excel.insert(loc = 5, column = 'cmplnc_revw_dt', value = temp)

                                                pdf_excel['gm_cmplnt_flg'] = 0

                                                pdf_excel.loc[pdf_excel['gm_cmplnt_dt'] == '', 'gm_cmplnt_flg'] = 'No'

                                                pdf_excel.loc[pdf_excel['gm_cmplnt_dt'] != '', 'gm_cmplnt_flg'] = 'Yes'

                                                # new column file_dt as identifier of what file was loaded

                                                pdf_excel.insert(loc=8, column = 'file_dt', value = file_dt)

                                                pdf_excel['gm_cmplnt_dt'] = pdf_excel['gm_cmplnt_dt'].str.replace('/','-')

                                                # dropping duplicates now that files are together

                                                pdf_excel['gm_cmplnt_dt'] = pdf_excel['gm_cmplnt_dt'].str.replace('2001-01-01', '')

                                                pdf_excel['cmplnc_revw_dt'] = pdf_excel['cmplnc_revw_dt'].str.replace('2001-01-01', '')

                                                pdf_excel['gm_cmplnt_dt'] = pdf_excel['gm_cmplnt_dt'].str.replace('20010101', '')

                                                pdf_excel['cmplnc_revw_dt'] = pdf_excel['cmplnc_revw_dt'].str.replace('20010101', '')

                                                pdf_excel = pdf_excel.replace(' ', 'test')

                                                pdf_excel['gm_cmplnt_dt'] = pdf_excel['gm_cmplnt_dt'].replace(['NULL', '', None, '<NA>', 'nan'], np.nan)

                                                pdf_excel.dropna(subset=['cmplnc_revw_dt', 'gm_cmplnt_dt'], how='all', inplace=True)

                                                # static value

                                                pdf_excel['src_sys_id'] = '171749'

                                               

                                                pdf_excel = pdf_excel.applymap(str)

                                               

                                                # Get current time stamp

                                                CURR_TS = datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f")

                                               

                                                if sdf is None:

                                                            sdf = spark.createDataFrame(pdf_excel)

                                                            sdf = sdf.withColumn("sched_qtr_nbr", sdf.sched_qtr_nbr.cast(IntegerType()))

                                                            sdf = sdf.withColumn("cmplnc_revw_dt", sdf.cmplnc_revw_dt.cast(DateType()))

                                                            sdf = sdf.withColumn("file_dt", sdf.file_dt.cast(DateType()))

                                                            sdf = sdf.withColumn("gm_cmplnt_dt", sdf.gm_cmplnt_dt.cast(DateType()))

                                                else:

                                                            sdf_new = spark.createDataFrame(pdf_excel)

                                                            sdf_new = sdf_new.withColumn("sched_qtr_nbr", sdf_new.sched_qtr_nbr.cast(IntegerType()))

                                                            sdf_new = sdf_new.withColumn("cmplnc_revw_dt", sdf_new.cmplnc_revw_dt.cast(DateType()))

                                                            sdf_new = sdf_new.withColumn("file_dt", sdf_new.file_dt.cast(DateType()))

                                                            sdf_new = sdf_new.withColumn("gm_cmplnt_dt", sdf_new.gm_cmplnt_dt.cast(DateType()))

                                                            sdf = sdf.union(sdf_new)

                        return sdf

 

#############################################################################

# READ DATA FROM S3

#############################################################################

# excel_to_sdf(bucket,searchPattern,num_of_columns,src_nm,skiprows)

bucket = "company"

bucket_archive = bucket + "-archive"

searchPattern = 'FacilityComplianceReview'

sdf = excel_to_sdf(bucket, searchPattern,0)

 

#############################################################################

# LOAD STAGING TABLE

#############################################################################

if sdf is not None:

            # WHEEL FILE DEPENDENCY

            # gm_hive.write_sdf_as_parque(spark,sdf,STAGING_TABLE_NAME,STAGING_TABLE_PATH,mode='append')

            sdf.write.mode("append").parquet(STAGING_TABLE_PATH)

 

print('File written to table')

 

 

#############################################################################

# MOVE THE SOURCE FILES INTO THE ARCHIVE

#############################################################################

# If this portion of code is ran in Jupyter, you need to move the files back to the original bucket and remove timestamps

# cmd copy arg: mc cp cvi_dev_admin/ArchiveBucketName/"File Name_timestamp" cvi_dev_admin/TargetBucketName/"File Name"

 

if sdf is not None:

            resp = s3_client.list_objects_v2(Bucket=bucket)

            contents = resp['Contents']

            for file_object in resp['Contents']:

                        filename=file_object.get('Key')

                        # SearchPattern

                        if searchPattern in filename:

                                    # WHEEL FILE DEPENDENCY

                                    # gm_s3.archive_files(s3_client, 'company','company-archive', filename)

                                    s3_client.copy_object(Bucket = bucket_archive, CopySource = f"{bucket}/{filename}", Key = f"{filename}")

                                    s3_client.delete_object(Bucket = bucket, Key = filename)

                                    print(f"{datetime.now().strftime('%Y-%m-%d %H:%M:%S')} | Succesfully archived \"{filename}\" in the following bucket: {bucket_archive}")

 

spark.stop()

sys.exit(0)
