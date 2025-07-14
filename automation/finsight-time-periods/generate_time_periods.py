__version__ = "1.0.0"
__author__ = "Dustin"
__description__ = "FinSight: Time Period Generator for Notion"

import os
import calendar
import json
from datetime import datetime, timedelta
from notion_client import Client
from notion_client.errors import APIResponseError
from dotenv import load_dotenv

load_dotenv()
token = os.environ["NOTION_TOKEN"]
print(f"🔧 {__description__} v{__version__} by {__author__}\n")
notion = Client(auth=os.environ["NOTION_TOKEN"])
DATABASE_ID = "21b21ae39a748019b4e3c5defc0f167d"
notionPayload = []
BIWEEKLY_ID = "Bi-Weekly"
MONTHLY_ID = "Monthly"
YEARLY_ID = "Yearly"
BUFFER = "buffer"
LAST = "last"
CURRENT = "current"
FUTURE = "future"
PERIOD_CONFIG = {
      BIWEEKLY_ID: {
          BUFFER: 2,
          LAST: None,
          CURRENT: None,
          FUTURE: []
      },
      MONTHLY_ID: {
          BUFFER: 1,
          LAST: None,
          CURRENT: None,
          FUTURE: []
      },
      YEARLY_ID: {
          BUFFER: 1,
          LAST: None,
          CURRENT: None,
          FUTURE: []
      }
  }
NAME_FORMAT = {
    BIWEEKLY_ID: "%m-%d-%Y",
    MONTHLY_ID: "%B %Y",
    YEARLY_ID: "%Y"
}

def get_monthly_end(date: datetime) -> datetime:
    _, last_day = calendar.monthrange(date.year, date.month)
    return datetime(date.year, date.month, last_day)

def get_biweekly_end(start: datetime) -> datetime:
    return start + timedelta(days=13)

def get_yearly_end(start: datetime) -> datetime:
    return datetime(start.year, 12, 31)

END_DATE_FUNCTIONS = {
    BIWEEKLY_ID: get_biweekly_end,
    MONTHLY_ID: get_monthly_end,
    YEARLY_ID: get_yearly_end
}
response = notion.databases.query(**{"database_id": DATABASE_ID})
schema = notion.databases.retrieve(database_id=DATABASE_ID)

currentTime = datetime.today().date()


def dbg(label, value, addColon):
  if addColon:
    print(f"[DEBUG] {label}: {value}\n")
    return
  print(f"[DEBUG] {label} {value}\n")


def parse_time_period(page):
  props = page["properties"]
  try:
    return {
        "name":
          props["Name"]["title"][0]["text"]["content"],
        "id":
          page["id"],
        "type":
          props["Type"]["select"]["name"],
        "start":
          datetime.strptime(props["Start Date"]["date"]["start"], "%Y-%m-%d"),
        "end":
          datetime.strptime(props["End Date"]["date"]["start"], "%Y-%m-%d"),
        "is_current": props["Is Current"]["checkbox"]
    }
  except Exception as e:
    dbg("Parse Error", str(e), True)
    return None

def create_time_period(name: str, type_name: str, start: str, end: str, is_current: bool = False):
    return {
            "Name": {
              "title": [{"text": {"content": name}}]
            },
            "Type": {
              "select": {"name": type_name}
            },
            "Start Date": {
              "date": {"start": start}
            },
            "End Date": {
              "date": {"start": end}
            },
            "Is Current": {
              "checkbox": is_current
            }
        }

def update_isCurrent_status(period, isCurrent):
  try:
    notion.pages.update(
          page_id=period["id"],
          properties={
            "Is Current": {"checkbox": isCurrent}
          }
      )
  except APIResponseError as e:
    dbg("Failed to update current status of period", e.message, True)

def is_future_period(period, today):
  if period["start"].date() > today and period["end"].date() > today:
    PERIOD_CONFIG[period["type"]][FUTURE].append(period)
    return True
  return False

def is_current_period(period, today):
  if period["start"].date() < today and period["end"].date() > today:
    PERIOD_CONFIG[period["type"]][CURRENT] = period
    if period["is_current"] == False:
       update_isCurrent_status(period, True)
    return True
  return False

def is_most_recent_period(period):
  if period and period["is_current"] == True:
     update_isCurrent_status(period, False)
  if PERIOD_CONFIG[period['type']][LAST] is None or PERIOD_CONFIG[period['type']][LAST]['end'].date() < period['end'].date():
      PERIOD_CONFIG[period['type']][LAST] = period
      return True
  return False

def find_periods():
  for page in response["results"]:
    period = parse_time_period(page)
    if not period:
      continue

    if is_future_period(period, currentTime):
      dbg(f"{period['type']} → Period In Future", period["name"], True)
      
    elif is_current_period(period, currentTime):
      dbg(f"{period['type']} → Period In Progress", period["name"], True)
      
    elif is_most_recent_period(period):
      dbg(f"{period['type']} → Most Recent Period not Current", period["name"], True)

def calculate_period_beginning_and_end(period: dict, isNotCurrent=False) -> dict:
    period_type = period["type"]
    end_func = END_DATE_FUNCTIONS.get(period_type)

    if not end_func:
        raise ValueError(f"Unsupported period type: {period_type}")

    new_start = period["end"] + timedelta(days=1)
    new_end = end_func(new_start)

    # If this is not the current period, keep calculating until we reach the first period
    # that includes the current time
    if isNotCurrent:
        while new_end.date() < currentTime:
            new_start = new_end + timedelta(days=1)
            new_end = end_func(new_start)

    return {
        "name": f"{period_type} {new_start.strftime(NAME_FORMAT[period_type])}",
        "type": period_type,
        "start": new_start,
        "end": new_end
    }


def generate_future_periods(period_type: str, buffer_count: int, last_period: dict):
  new_periods = []
  current = last_period

  while (len(new_periods) + len(PERIOD_CONFIG[period_type][FUTURE])) < buffer_count:
    next_period = calculate_period_beginning_and_end(current)
    current = next_period
    if any(p["start"] == next_period["start"] for p in PERIOD_CONFIG[period_type][FUTURE]):
            continue
    notionPayload.append({
            "parent": {"database_id": DATABASE_ID},
            "properties": create_time_period(
               next_period["name"],
               period_type,
               next_period["start"].strftime("%Y-%m-%d"),
               next_period["end"].strftime("%Y-%m-%d"),
               is_current=False
            )
         })
    new_periods.append(next_period)
    dbg(f"Created new {period_type} Period", json.dumps(next_period, indent=2, default=str), True)

def ensure_current_period(period_type):
  if not PERIOD_CONFIG[period_type][CURRENT]:
    if PERIOD_CONFIG[period_type][LAST]:
      PERIOD_CONFIG[period_type][CURRENT] = calculate_period_beginning_and_end(
        PERIOD_CONFIG[period_type][LAST],
        True
      )
      notionPayload.append(
         {
            "parent": {"database_id": DATABASE_ID},
            "properties": create_time_period(
               PERIOD_CONFIG[period_type][CURRENT]["name"],
               period_type,
               PERIOD_CONFIG[period_type][CURRENT]["start"].strftime("%Y-%m-%d"),
               PERIOD_CONFIG[period_type][CURRENT]["end"].strftime("%Y-%m-%d"),
               is_current=True
            )
         }
      )
      dbg(f"Created Missing Current {period_type} Period", json.dumps(PERIOD_CONFIG[period_type][CURRENT], indent=2, default=str), True)
    else:
      raise AttributeError("No current or past time periods could be found. Cannot create new periods without reference.")

def main():
  find_periods()

  for period_type, config in PERIOD_CONFIG.items():
    try:
      ensure_current_period(period_type)
    except Exception as e:
      print(f"An unexpected error occured: {e}")
      return
    
    if period_type == YEARLY_ID and currentTime.month < 10:
      continue
    generate_future_periods(
        period_type,
        config[BUFFER],
        config[CURRENT]
      )

  for page in notionPayload:
    try:
        notion.pages.create(**page)
    except APIResponseError as e:
        dbg("Notion API Error", e.message, True)

main()

dbg("Total Future Bi-Weekly Periods Found", len(PERIOD_CONFIG[BIWEEKLY_ID][FUTURE]), True)
dbg("Total Future Monthly Periods Found", len(PERIOD_CONFIG[MONTHLY_ID][FUTURE]), True)
dbg("Total Future Yearly Periods Found", len(PERIOD_CONFIG[YEARLY_ID][FUTURE]), True)