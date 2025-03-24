import json, argparse

class Report:
    def __init__(self, data):
        self.stacktrace = data['STACK_TRACE']
        self.version = data['APP_VERSION_NAME']

    def __hash__(self):
        return hash(self.stacktrace)

    def __eq__(self, other):
        return self.stacktrace == other.stacktrace


def process_reports(input_file, output_file, version=None, only_errors=None):
    try:
        with open(input_file, 'r') as file:
            data = json.load(file)
        
        reports = [Report(entry) for entry in data]
        if version and isinstance(version, str):
            reports = [report for report in reports if report.version.strip() == version.strip()]

        report_count, unique_reports = count_unique_reports(reports)
        if only_errors:
            unique_reports = filter_unique_exceptions(unique_reports)
        
        # Convert the set back to list of dictionaries
        processed_data = [{'stacktrace': r.stacktrace, 'version': r.version, 'count': report_count[r.stacktrace]} for r in unique_reports]
        processed_data = sorted(processed_data, key=lambda x: x['count'], reverse=True)

        with open(output_file, 'w') as file:
            json.dump(processed_data, file, indent=4)
        
        print("File processed successfully.")
    
    except FileNotFoundError:
        print(f"Error: The file {input_file} does not exist.")
    except json.JSONDecodeError:
        print("Error: The file is not a valid JSON.")
    except Exception as e:
        print(f"An error occurred: {e}")


def count_unique_reports(reports):
    report_dict = {}
    for report in reports:
        exception_name = report.stacktrace.split(':')[0].strip()
        if exception_name in report_dict:
            report_dict[exception_name]['count'] += 1
        else:
            report_dict[exception_name] = {'report': report, 'count': 1}
    
    unique_reports = [v['report'] for v in report_dict.values()]
    report_count = {v['report'].stacktrace: v['count'] for v in report_dict.values()}
    return report_count, unique_reports


def filter_unique_exceptions(reports):
    seen_exceptions = set()
    unique_reports = []

    for report in reports:
        exception_name = report.stacktrace.split(':')[0].strip()
        
        # Check if this exception name has already been processed
        if exception_name not in seen_exceptions:
            seen_exceptions.add(exception_name)
            unique_reports.append(report)
    
    return unique_reports


# Define the input and output files
input_filename = 'reports.json'
output_filename = 'output.json'


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--version", default=None)
    parser.add_argument("--only-errors", action="store_true")
    args = parser.parse_args()

    version = args.version if args.version else None
    only_errors = args.only_errors if args.only_errors else None
    process_reports(input_filename, output_filename, version, only_errors)


main()
