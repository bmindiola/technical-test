FROM python:3.10-slim

WORKDIR /app

COPY send_orders.py /app/send_orders.py
COPY requirements.txt /app/requirements.txt

RUN pip install --no-cache-dir -r requirements.txt

CMD ["python", "send_orders.py"]
