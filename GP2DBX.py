# Formatting print

BOLD = '\033[1m'

RED = '\033[91m'

BLUE = '\033[94m'

GREEN = '\033[92m'

MAGENTA = '\033[95m'

YELLOW = '\033[93m'

RESET = '\033[0m'

SIERRA_GOLD  = "\033[38;2;205;179;139m"

 

 

import re

print(f"         {BOLD}{RED}STARTING GREENPLUM TO DATABRICKS CONVERSION{RESET}")

 

from databricks import sql

print(f"{BOLD}{SIERRA_GOLD}Engines Starting...{RESET}")

import pandas as pd

print(f"{BOLD}{SIERRA_GOLD}Batteries Starting...{RESET}")

import sys

import time

from datetime import datetime, timedelta

 

 

# Progress Bar

def progress_bar(iteration, total, prefix='', suffix='', length=30, fill='█', print_end="\r"):

    """

    Call in a loop to create terminal progress bar

   

    Parameters:

        iteration (int): Current iteration (required)

        total (int): Total iterations (required)

        prefix (str): Prefix string (optional)

        suffix (str): Suffix string (optional)

        length (int): Character length of bar (optional)

        fill (str): Bar fill character (optional)

        print_end (str): End character (optional), defaults to carriage return '\r' to overwrite the line

    """

    percent = ("{0:.1f}").format(100 * (iteration / float(total)))

    filled_length = int(length * iteration // total)

    bar = fill * filled_length + '-' * (length - filled_length)

    sys.stdout.write('\r%s |%s| %s%% %s' % (prefix, bar, percent, suffix))

    sys.stdout.flush()

    # Print New Line on Complete

    if iteration == total:

        print()

 

 

# Example usage:

total_iterations = 100

for i in range(total_iterations + 1):

    time.sleep(0.1)  # Simulate some work

    progress_bar(i, total_iterations, prefix='Progress:', suffix='Complete', length=50)

 

print(f"{BOLD}{GREEN}Processing completed!{RESET}")

 

server_hostname = "adb-*************.azuredatabricks.net"

http_path       = "/sql/1.0/warehouses/**********"

access_token    = "************************"

 

connection = sql.connect(

                        server_hostname = server_hostname,

                        http_path = http_path,

                        access_token = access_token

                        )

 

cursor = connection.cursor()

 

## Insert Your Query within the 3 Quotes like shown below

def multiline_input(prompt="Enter text (terminate with an empty line):\n"):

    lines = []

    print(prompt)

    while True:

        line = input()

        if line:

            lines.append(line)

        else:

            break

    return '\n'.join(lines)

   

query = multiline_input("""Enter your SQL query and press enter two times: """)

 

######################################################################################################################

 


 

# Defining the Function

def replace_pattern(match, keyword, first_source, second_source, query):

    full_match = match.group()

    schema_table = re.search(r'\w+\.\w+', full_match).group()

    schema = re.search(r'\w+', schema_table).group()

    table = re.search(r'\.\w+', schema_table).group()

   

    try:

        replace = f"{keyword} {first_source}.{schema_table}"

        test_query = f"select * from {first_source}.{schema_table} limit 1;"

        cursor.execute(test_query).fetchall()

        query = query.replace(full_match, replace)

        print(f"{schema_table} {BOLD}{BLUE} IN {first_source}{RESET}")

    except:

        try:

            replace = f"{keyword} {second_source}.{schema_table}"

            test_query = f"select * from {second_source}.{schema_table} limit 1;"

            cursor.execute(test_query).fetchall()

            query = query.replace(full_match, replace)

            print(f"{schema_table} {BOLD}{GREEN} IN {second_source}{RESET}")

        except:

            try:

                replace = f"{keyword} {first_source}.{schema}_public{table}"

                test_query = f"select * from {first_source}.{schema}_public{table} limit 1"

                cursor.execute(test_query).fetchall()

                query = query.replace(full_match, replace)

                print(f"{schema}_public{table} {BOLD}{MAGENTA} IN {first_source}{RESET}")

            except:

                try:

                    replace = f"{keyword} {first_source}.{schema}_secure{table}"

                    test_query = f"select * from {first_source}.{schema}_secure{table} limit 1"

                    cursor.execute(test_query).fetchall()

                    query = query.replace(full_match, replace)

                    print(f"{schema}_secure{table} {BOLD}{YELLOW} IN {first_source}{RESET}")

                except:

                    print(f"{schema_table} {BOLD}{RED} NOT in {first_source} or {second_source}{RESET}")

       

 

    return query

 

# Copy the original query to a new variable and convert "YYYY" to "yyyy"

new_query = query.replace('YYYY', 'yyyy').replace('DD', 'dd')

 

# Define the patterns for finding "from ___.___" and "join ___.___ and "as '___'"

from_pattern = re.compile(r'from\s+\w+\.\w+', re.IGNORECASE)

join_pattern = re.compile(r'join\s+\w+\.\w+', re.IGNORECASE)

as_pattern = re.compile(r'as\s+\"(.+?)\"', re.IGNORECASE)

 

# Find all occurrences of the patterns in the query

from_matches = from_pattern.finditer(new_query)

join_matches = join_pattern.finditer(new_query)

as_matches = as_pattern.finditer(new_query)

 

#Converting citext to lower(string())

pattern = 'cast(.+as citext)'

new_query = re.sub(pattern, lambda match:f"lower({match.group()})", new_query)

new_query = re.sub('citext', 'string', new_query, flags = re.IGNORECASE)

 

# Replace Quotes with Backticks and varchar/text with string

new_query = new_query.replace('"', '`')

new_query = re.sub('varchar', 'string', new_query, flags = re.IGNORECASE)

new_query = re.sub('text', 'string', new_query, flags = re.IGNORECASE)

 

 

# Replaces char() or to_char() issues

if not re.search(r'char\([^,]+\,[^,]+\)', new_query):

    # If the pattern is not present, perform the replacement

    new_query = re.sub('to_char', 'char', new_query, flags=re.IGNORECASE)

else:

    # If the pattern is present, do something else (you mentioned "skip")

    new_query = re.sub('char', 'to_char', new_query, flags=re.IGNORECASE)

 

#Double check if this is needed

new_query = re.sub('to_char', 'char', new_query, flags = re.IGNORECASE)

 

# Iterate over the 'from' and 'join' matches and replace each one

for from_match in from_matches:

    new_query = replace_pattern(from_match, 'from', 'gmdataassets', 'reporting', new_query)

 

for join_match in join_matches:

    new_query = replace_pattern(join_match, 'join', 'gmdataassets', 'reporting', new_query)

 

# Adds date() to to_date extract to prevent format issues in parser

# Find all occurrences of to_date(...)

if 'to_date' in new_query:

    search = re.findall('to_date(.*?)(?=,|$)', new_query)

   

    # Iterate through each match

    for match in search:

        if 'extract' in match:

            if 'date(' not in match:  # If 'date(' is not already present

                # Replace to_date with to_date(date(

                new_query = re.sub(re.escape(match), f"(date{match})", new_query)

            else:

                # Keep the original to_date expression

                new_query = new_query.replace(f"to_date({match})", f"to_date({match}))")

 

# Replace the string_agg function with CONCAT_WS and COLLECT_SET and remove from dual sections

pattern = r"string_agg\(distinct trim\(([^)]+)\),', ' order by trim\(\1\)\)"

new_query = re.sub(pattern, r"CONCAT_WS(', ', COLLECT_SET(TRIM(\1)))", new_query)

new_query = re.sub("from dual","", new_query, flags=re.IGNORECASE)

 

# Replacing dt - dt with int(dt-dt)

pattern = "\w+\.\w+dt - \w+\.\w+|\w+\.\w+dt-\w+\.\w+|\w+date.\-.\w+\.\w+"

new_query = re.sub(pattern, lambda match: f"int({match.group()})", new_query)

 

# Print the modified query without spaces at start

new_query = new_query.lstrip()

 

# Write the modified query to a file

Name_file = input("Enter your filename here (e.g. Job_dbx): ")

with open(f"{Name_file}.txt", 'w') as f:

   f.write(new_query)

 

curr_TS = (datetime.now() + timedelta(hours=6)).strftime("%Y-%m-%d %H:%M:%S")

print(f"The New Query loaded at {curr_TS} CST, wait a few seconds to download.")

print(f"\n{new_query}")

######################################################################################################################

 
