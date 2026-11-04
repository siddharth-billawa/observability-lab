FROM python:3.13-alpine@sha256:7984cf56ceaddebe4fefeb75d64000b9f45d8b77b11a09b24524da6cdc3af0c8
WORKDIR /app
COPY sink.py .
EXPOSE 8090
CMD ["python", "sink.py"]
