FROM python:3.9.6-alpine

RUN apk update && apk add --virtual deps \
    gcc \
    linux-headers \
    musl-dev \
    postgresql-dev

RUN apk add --no-cache \
    libpq

COPY requirements.txt .
RUN pip install --upgrade pip \
    && pip install -r requirements.txt

VOLUME /app
WORKDIR /app